package de.rrze.guery

import de.rrze.guery.base.QueryBaseBuilder
import de.rrze.guery.policy.Policy
import de.rrze.guery.policy.PolicyRegistry;


class RulesController {

	static queryBase
	
	
	
    def index() {
		if (!queryBase) {
			redirect(action:'init')
			return
		}
		
		def jsonConfig = queryBase.toJson().toString(true)
		log.info("JSON config: ${jsonConfig}")
		
		[builderConfig:jsonConfig]
	}
	
	def save(String queryBuilderResult) {
		def policy = new Policy(queryBase, queryBuilderResult)
		
		
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
			
			policy.evaluate(req) { res ->
				println "RESULT:" + res
			}
			
			
//		}
		
		
		
	}
	
	def init() {
		log.info("Initializing query options ...")
		
		def idmGroupMap = [
			"initial"	: "initial",
			"activated"	: "activated",
			"active"	: "active",
			"inactive"	: "inactive",
			"archive"	: "archive",
		]
		
		queryBase = new QueryBaseBuilder().make {
			sortable true
			
			filter(id:'policy') {
				applicable { val, req -> PolicyRegistry.get(val).evaluate(req) }
			}
			
			filter(id:"user") {
				uidEqual { val, req -> val == req.environment.user?.uid }
			}
			
			filter(id:"entitlement") {
				typeEqual { val, req -> req.environment.entitlements?.findAll { it.type == val } }
				uidEqual { val, req -> req.environment.entitlements?.find { it.uid == val } }
				uidEqualsUser(accept_values:false) { req -> req.environment.entitlements?.findAll { it.uid == req.environment.user?.uid } }
			}
			
			filter(id:"groupMembership", input:'select', values:idmGroupMap) { 
				equal { val, req ->	req.environment.user?.groupMembership.contains(val) }
				exist(accept_values:false) { req -> req.environment.user?.groupMembership as Boolean }
			}
				
						
		}
		
		redirect(action:'index')
	}
	
}
