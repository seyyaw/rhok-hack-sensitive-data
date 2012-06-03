
importPackage(org.rhok.refine.commands);
var html = "text/html";
var encoding = "UTF-8";
var ClientSideResourceManager = Packages.com.google.refine.ClientSideResourceManager;

var RefineServlet = Packages.com.google.refine.RefineServlet;

/*
 * Function invoked to initialize the extension.
 */
function init() {

 RefineServlet.registerCommand(module, "CheckDataSensitivity", new ControlPrivacy()); 
 

  
  ClientSideResourceManager.addPaths(
    "project/scripts",
    module,
    [
      "scripts/project-injection.js"
    ]
  );

}

