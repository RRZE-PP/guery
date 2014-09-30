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
		def req = [user:user]
		println "Request: " + req
		
		def policyApplied =  policy.evaluate(req) {
			user.admin = true
		}
		println "Policy applied: " + policyApplied
		println "Output: " + user
		
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
				applicable { val, obj -> PolicyRegistry.get(val).evaluate(obj) }
			}
			
			filter(id:"username") {
				equal { val, obj, id ->	
					println id + " called!"
					return val == obj?.user?.uid 
				}
			}
			
			filter(id:"idmGroupMembership", input:'select', values:idmGroupMap) { 
				equal { val, obj ->	obj?.user?.groupMembership.contains(val) }
				exist(accept_values:false) { val, obj -> obj?.user?.groupMembership as Boolean }
			}
				
						
		}
		
		redirect(action:'index')
	}
	
}
