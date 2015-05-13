package de.rrze.guery

import de.rrze.guery.converters.JavascriptCode


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
			
			filter(id:"policy") {
				evaluate { val, obj -> gueryInstance.getPolicy(val).evaluate(obj) }
			}
			
			filter(id:"user") {
				uidEqual { val, req -> val == req.environment.user?.uid }
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
				equal { val, req ->	req.environment.user?.groupMembership.contains(val) }
				exist(accept_values:false) { req -> req.environment.user?.groupMembership as Boolean }
			}
		}
		
		def jsConfig = gueryInstance.baseToJsString(true)
		log.info("JS config: ${jsConfig}")
		
		
		log.info("${gueryInstance.getFilters()}")
		
		redirect(action:'index')
	}
	
	
	
	def save(String queryBuilderResult) {
		gueryInstance.makePolicyFromJson('test', queryBuilderResult)
		
		
		
		
		def user = [uid:'asd']
		
		def entitlements = [
			[type: 'EntitlementAD', uid:'grr'],
			[type: 'EntitlementAD', uid:'asd'],
			[type: 'EntitlementAccount', uid:'grr'],
			[type: 'EntitlementAccount', uid:'asd'],
			]
		
		def msg = [user:user, entitlements:entitlements]
		
//		msg.entitlements.each {
			
			def req = [
//				resource	: it, 
				environment	: msg,
//				subject		: null,
//				action		: null,
			]
			
			gueryInstance.getPolicy('test').evaluate(req) { res ->
				println "RESULT:" + res
			}
			
			
//		}
		
		
	}
	
	
	
}
