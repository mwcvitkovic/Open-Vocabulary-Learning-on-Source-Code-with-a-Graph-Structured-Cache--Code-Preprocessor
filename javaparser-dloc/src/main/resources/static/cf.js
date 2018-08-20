function convert(json) {
    var keys= {
        '_id': 'id',
        'type': 'group',
        '_outV': 'source',
        '_inV': 'target'
    };
    $.each(json.vertices ,function(pos,obj){
        var counter = 0;
        $.each(obj,function(key,value){
            if (key in keys) {
                json.vertices[pos][keys[key]] = value;
                delete json.vertices[pos][key];
                counter++;
            }
        })
    })
    $.each(json.edges ,function(pos,obj){
        var counter = 0;
        $.each(obj,function(key,value){
            if (key in keys) {
                json.edges[pos][keys[key]] = value;
                delete json.edges[pos][key];
                counter++;
            }
        })
    })
    var data = {
        nodes: json.vertices,
        links: json.edges
    };
    return data;
}

function draw(val) {
    d3.json(val, function(error, data) {
        if (error) throw error;
        graph = convert(data);

        var mLinkNum = {};

        var svg = d3.select("svg");
        svg.selectAll("*").remove();


        var g = svg.append("g")
            .attr("class", "everything");

        svg.append("defs").selectAll("marker")
            .data(["end"])      // Different link/path types can be defined here
          .enter().append("marker")    // This section adds in the arrows
            .attr("id", String)
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 15)
            .attr("refY", 0.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
          .append("path")
            .attr("d", "M0,-5L10,0L0,5")
            .attr('fill', '#999');

        function updateWindow(){
            var w = window;
            width = w.innerWidth || e.clientWidth || g.clientWidth;
            height = w.innerHeight|| e.clientHeight|| g.clientHeight;

            svg.attr("width", width).attr("height", height);
        }
        updateWindow();

        var color = d3.scaleOrdinal(d3.schemeCategory20b);
        var link_color = d3.scaleOrdinal(d3.schemeCategory10);;

        var simulation = d3.forceSimulation()
            .force("link", d3.forceLink().id(function(d) { return d.id; }))
            .force("charge", d3.forceManyBody())
            .force("center", d3.forceCenter(width / 2, height / 2));

        var path = g.append("g")
            .selectAll("line")
            .data(graph.links)
            .enter().append("path")
            .style("stroke-width", function (d) { return 2; })
            .attr("stroke", function(d) { return link_color(d._label);  })
            .attr("marker-end", "url(#end)");

        var node = g.append("g")
            .attr("class", "nodes")
            .selectAll("g")
            .data(graph.nodes)
            .enter().append("g");

        node.append("circle")
            .attr("r", 10)
            .attr("fill", function(d) { return color(d.group); })
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended));

        node.append("title")
            .text(function(d) { return "identifier: " + d.identifier + "\n" +
                                       "modifier: " + d.modifier + "\n" +
                                       "reference: " + d.reference + "\n" +
                                       "-------------------------------------------" + "\n" +
                                       d.text; });

        node.append("text")
            .text(function(d) { return d.group; })
            .attr('dy', -10);

        simulation
          .nodes(graph.nodes)
          .on("tick", ticked);

        simulation.force("link")
          .links(graph.links);

        var zoom_handler = d3.zoom()
           .on("zoom", zoom_actions);

        zoom_handler(svg);

        function ticked() {
            path.attr("d", function (d) {
                return 'M ' + d.source.x + ' ' + d.source.y + ' L ' + d.target.x + ' ' + d.target.y;
            });

            // Add tooltip to the connection path
            path.append("title")
                .text(function (d, i) { return d.name; });

            node
                .attr("transform", function(d) {
                  return "translate(" + d.x + "," + d.y + ")";
                })
        }

        function zoom_actions(){
            g.attr("transform", d3.event.transform)
        }


        function dragstarted(d) {
            if (!d3.event.active) simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
        }

        function dragended(d) {
            if (!d3.event.active) simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }
    })
}
