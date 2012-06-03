$(function() {
	$(document).ready(
			function() {

				var checkSensitive = function() {
					Refine.postProcess("Rhok-extension",
							"CheckDataSensitivity", params = {}, {}, {}, {
								"onDone" : function(response) {
									alert(response["key"]);
								}
							});
				}

				ExporterManager.handlers.showCheckSensitiveDialog = function() {
					var dialog = $(DOM.loadHTML("Rhok-extension",
							"html/rhok-dialog.html"));

					var elmts = DOM.bind(dialog);
					var level = DialogSystem.showDialog(dialog);

					elmts.okButton.click(function() {
						checkSensitive();
						DialogSystem.dismissUntil(level - 1);
					});
					elmts.cancelButton.click(function() {
						DialogSystem.dismissUntil(level - 1);
					});
				};

				$(function() {
					ExtensionBar.MenuItems.push({
						"id" : "Rhok", // uploadtoENS
						"label" : "Rhok",
						"submenu" : [ {
							"id" : "Rhok-Check-Sensitive-Data",
							label : "Check Sensitive Data...",
							click : function() {
								ExporterManager.handlers
										.showCheckSensitiveDialog();
							}
						} ]
					});
				});

			});
});