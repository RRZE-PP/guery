class GueryGrailsPlugin {
    // the plugin version
    def version = "0.4.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/**",
        "grails-app/controllers/**",
    ]

    def title = "Guery Plugin" // Headline display name of the plugin
    def author = "Florian Löffler"
    def authorEmail = "florian.loeffler@fau.de"
    def description = '''\
Grails plugin for building and evaluating queries/rules using the execellent jQuery Query Builder GUI as a frontend.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/guery"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Regional Computing Center Erlangen (RRZE)", url: "http://www.rrze.fau.de/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ 
		[ name: "Florian Löffler", email: "florian.loeffler@fau.de" ],
		[ name: "Frank Tröger", email: "frank.troeger@fau.de" ],
		[ name: "Sven Marschke", email: "sven.marschke@fau.de" ],
	]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "github", url: "https://github.com/RRZE-PP/guery/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/RRZE-PP/guery" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
