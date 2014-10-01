package de.rrze.guery

import de.rrze.guery.base.QueryBaseBuilder
import de.rrze.guery.policy.Policy


class PolicyController {

	static gueryInstance
	
	
	
    def index() {
		if (!gueryInstance) {
			redirect(action:'init')
			return
		}
		
		def jsonConfig = gueryInstance.baseToJson().toString(true)
		log.info("JSON config: ${jsonConfig}")
		
		[builderConfig:jsonConfig]
	}
	
	def save(String queryBuilderResult) {
		gueryInstance.makePolicyFromJson('test', queryBuilderResult)
		
		
		
		
		def user = [uid:'asd']
		def req = [user:user]
		println "Request: " + req
		
		def policyApplied = gueryInstance.getPolicy('test').evaluate(req) {
			user.admin = true
		}
		println "Policy applied: " + policyApplied
		println "Output: " + user
		
	}
	
	def init() {
		log.info("Initializing gueryInstance ...")
		gueryInstance = new GueryInstance()
		
		def idmGroupMap = [
			"initial"	: "initial",
			"activated"	: "activated",
			"active"	: "active",
			"inactive"	: "inactive",
			"archive"	: "archive",
		]
		
		gueryInstance.makeBase {
			sortable true
			
			filter(id:'policy') {
				evaluate { val, obj -> gueryInstance.getPolicy(val).evaluate(obj) }
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
