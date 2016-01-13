var getChart = function(divName,title,titleY,datasrc,data_) {
  if(typeof(data_)=='undefined') {
      data_ = [];
  }
  for(var i=0; i<datasrc.length; i++) {
    data_.push({
      type: "line",
      showInLegend: true,
      name: "Route " + (i+1),
      dataPoints: datasrc[i]
    });
  }
  var chart = new CanvasJS.Chart(divName,
  {
    title:{
      text: title 
    },
    data: data_,
    zoomEnabled: true,
    zoomType: "xy",
    exportFileName: title,
    exportEnabled: true,
    axisX:{
      title: "Iteration #",
    },
    axisY:{
      title: titleY,
    },
    legend: {
      cursor: "pointer",
      itemclick: function (e) {
        if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
          e.dataSeries.visible = false;
        } else {
          e.dataSeries.visible = true;
        }
        chart.render();
      }
    }
  });
  return chart;
};

var getAvgCost = function() {
    var avgCost = [];
    for(var j=0; j<cars[0].length; j++) {
        total = 0.0;
        numcars = 0;
        for(var i=0; i<cars.length; i++) {
            total += cars[i][j].y*costs[i][j].y;
            numcars += cars[i][j].y;
        }
        avgCost[j] = {'x':j,'y':total/numcars};
    }

    return {
        type: "line",
        showInLegend: true,
        name: "Average Cost",
        color: "black",
        dataPoints: avgCost
    };
};

var createDataTable = function(divID, data,tbltitle) {
  var div = document.getElementById(divID);
  var table = document.createElement("table");
  var head = table.createTHead();
  var title = head.insertRow().insertCell(0);
  title.colSpan = data.length+1;
  title.className = "title";
  title.appendChild(document.createTextNode(tbltitle));
  var headrow = head.insertRow();
  headrow.insertCell(0).appendChild(document.createTextNode("Iteration #"));
  for(var i=0; i<data.length; i++) {
    headrow.insertCell(i+1).appendChild(document.createTextNode("Route #"+(i+1)));
  }
  var body = table.createTBody();
  for(var i=0; i<data[0].length; i++) {
    var row = body.insertRow();
    row.className="alt"+(i%2);
    row.insertCell(0).appendChild(document.createTextNode(i+1));
    for(var j=0; j<data.length; j++) {
      row.insertCell((j+1)).appendChild(document.createTextNode(data[j][i].y));
    }
  }
  div.appendChild(table);
};

var createParamTable = function(divID,params) {
    var div = document.getElementById(divID);
    var table = document.createElement("table");
    var head = table.createTHead();
    var title = head.insertRow().insertCell(0);
    title.colSpan = 2;
    title.className = "title";
    title.appendChild(document.createTextNode("Simulation Parameters"));
    var headrow = head.insertRow();
    headrow.insertCell(0).appendChild(document.createTextNode("Parameter"));
    headrow.insertCell(1).appendChild(document.createTextNode("Value"));
    var body = table.createTBody();
    for(var i=0; i<params.length; i++) {
      var row = body.insertRow();
      row.className="alt"+(i%2);
      var key = params[i].x;
      var val = params[i].y;
      row.insertCell(0).appendChild(document.createTextNode(key));
      row.insertCell(1).appendChild(document.createTextNode(val));
    }
    div.appendChild(table);
};

window.onload = function () {
  getChart("carsContainer","# Cars per Route over time","# Cars",cars).render();
  getChart("costsContainer","Costs per Route over time","Cost per Car",costs,[getAvgCost()]).render();
  getChart("trpfContainer","Traffic Route Preference Function over time","TRPF",trpfs).render();
  createDataTable("carsTable",cars,"# Cars per Route");
  createDataTable("costsTable",costs,"Cost per Route");
  createDataTable("trpfTable",trpfs,"Traffic Route Preference Function");
  createParamTable("paramContainer",params);
}
