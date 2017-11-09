package de.rrze.guery

import org.grails.plugin.guery.GueryInstance


class PolicyController {

	static gueryInstance
	
	
	
    def index() {
		if (!gueryInstance) {
			redirect(action:'init')
			return
		}
		
//		def jsConfig = gueryInstance.baseToJsString(true)
//		log.info("JS config: ${jsConfig}")
		
		[gueryInstance:gueryInstance]
	}
	
	def resources() {
		if (!gueryInstance) {
			redirect(action:'init')
			return
		}
		
		def jsConfig = gueryInstance.baseToJsString(true)
		log.info("JS config: ${jsConfig}")
		
		[builderConfig:jsConfig]
	}
	
	
	
	
	def init() {
		log.info("Initializing gueryInstance ...")
		
		def baseInstance = new GueryInstance('base')
		baseInstance.buildBase {
			sortable true
			filterDescription true
//			conditions = ['elementMatch']
			
			filter(id:"policy", label:"Richtlinie", description:"Starke Richtlinie!") {
				evaluate { val, req -> gueryInstance.getPolicy(val).evaluate(req) }
			}
			
			filter(id:"user.uid") {
				equal { val, req -> val == req.environment.user?.uid }
			}
			
			
		}
		
		
		gueryInstance = new GueryInstance('extended',baseInstance)
		
		def idmGroupMap = [
			"initial"	: "initial",
			"activated"	: "activated",
			"active"	: "active",
			"inactive"	: "inactive",
			"archive"	: "archive",
		]
		
		gueryInstance.buildBase {
			sortable true
			
			filter(id:"entitlement") {
				typeEqual { val, req -> req.environment.entitlements?.findAll { it.type == val } }
				uidEqual { val, req -> req.environment.entitlements?.findAll { it.uid == val } }
				uidEqualsUser(accept_values:false) { req -> req.environment.entitlements?.findAll { it.uid == req.environment.user?.uid } }
			}
			
//			filter(id:"groupMembership", input:'select', values:idmGroupMap, onAfterSetValue:"console.log('hoho')") {
			
				
			filter(id:"groupMembership", input:'select', values:idmGroupMap) {
				equal(mongo:"v[0]") { val, req ->	req.environment.user?.groupMembership.contains(val) }
				exist(accept_values:false) { req -> req.environment.user?.groupMembership as Boolean }
			}
		}
		
//		def jsConfig = gueryInstance.baseToJsString(true) // pretty print does not work with mongoOperators
		def jsConfig = gueryInstance.baseToJsString(false)
		log.info("JS config: ${jsConfig}")
		
		
		log.info("${gueryInstance.getFilters()}")
		
		redirect(action:'index')
	}
	
	
	
	def save(String guery_builder_extended_result) {
		def policy = gueryInstance.parsePolicyFromJson(guery_builder_extended_result)
		
		
		
		
		def user = [uid:'asd']
		
		def entitlements = [
			[type: 'EntitlementAD', uid:'grr'],
			[type: 'EntitlementAD', uid:'asd'],
			[type: 'EntitlementAccount', uid:'grr'],
			[type: 'EntitlementAccount', uid:'asd'],
			]
		
		def msg = [user:user, entitlements:entitlements]
		
			
		def req = [
			environment	: msg,
		]
		
		// guery
		policy.id = "test"
		gueryInstance.putPolicy(policy)
		gueryInstance.evaluate(req) { res ->
			println "A RESULT:" + res
		}
		
		// single policy
		policy.evaluate(req) { res ->
			println "B RESULT:" + res
		}
		
		
		redirect(action:"index")
		
	}
	
	
	
}
