grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]


def live = System.getProperty("live")?System.getProperty("live").split(','):[]

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

//        mavenLocal()
        mavenCentral()

		
		def ppRepo = grailsSettings.config.grails.project.repos.ppArtifactory
		mavenRepo(id:ppRepo.id , url:ppRepo.url) {
			updatePolicy 'always'
			auth([username: ppRepo.username,password: ppRepo.password])
		}
		
		def localRepo = grailsSettings.config.grails.project.repos.localArtifactory
		mavenRepo(id:localRepo.id , url:localRepo.url) {
			updatePolicy 'always'
			auth([username: localRepo.username,password: localRepo.password])
		}
		
    }

    dependencies {
    }

    plugins {
		if (live.find { it == 'guery' }) {
			println "[GUERY] Running in live-mode!"
		}
		else {
			build(	":tomcat:7.0.55.2",
					":release:3.1.1",
			) {
			  export = false
		    }
			
			compile (":jquery-ui:1.10.4") {
				excludes "jquery"
				export = false
			}
			
			compile (
				":resources:1.2.14",
				":asset-pipeline:2.1.5",
				":jquery:1.11.1"
			) {
				export = false
			} 
		}
    }
}
