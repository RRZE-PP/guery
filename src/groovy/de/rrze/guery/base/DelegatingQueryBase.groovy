package de.rrze.guery.base

import java.util.List;

import de.rrze.guery.converters.Javascript
import de.rrze.guery.operator.ClosureOperationManager
import de.rrze.guery.operator.DelegatingClosureOperationManager
import de.rrze.guery.operator.IOperationManager
import de.rrze.guery.operator.Operator

class DelegatingQueryBase extends QueryBase {

	QueryBase parent
	
	IOperationManager		operationManager = new DelegatingClosureOperationManager()
	
	
	
	def DelegatingQueryBase() {
		super()
	}
	
	def DelegatingQueryBase(QueryBase parentQb) {
		super()
		parent = parentQb
		operationManager.parent = parentQb.operationManager
	}
	
	
	Map<String,Operator> getOperators() {
		getMergedFieldValue('_operators')
	}
	
	Map<String,Filter> getFilters() {
		getMergedFieldValue('_filters')
	}
	
	Map<String,String> getLang() {
		getMergedFieldValue('_lang')
	}
	
	Boolean getSortable() {
		getMergedFieldValue('_sortable')
	}
	
	Map<String,Boolean> getReadonlyBehaviour() {
		getMergedFieldValue('_readonlyBehaviour')
	}
	
	List<String> getConditions() {
		getMergedFieldValue('_conditions')
	}
	
	String getDefaultCondition() {
		getMergedFieldValue('_defaultCondition')
	}
	
	
	
	def getMergedFieldValue(String fieldName) {
		def retValue
		def tmpValue = this."${fieldName}"
		
		if (parent) {
			if (tmpValue == null) { // if there is a parent and the stored value is empty
				retValue = parent."${fieldName}"
			}
			else { // if there is a parent and the stored value is NOT empty
				if (tmpValue in Map) { // merge with parent map
					retValue = [:]
					def parentValue = parent."${fieldName}"
					if (parentValue) retValue.putAll(parentValue)
					retValue.putAll(tmpValue) // current overrides parent
				}
				else if (tmpValue in Collection) { // merge with parent collection
					retValue = []
					def parentValue = parent."${fieldName}"
					if (parentValue) retValue.addAll(parentValue)
					retValue.addAll(tmpValue) // current overrides parent
				}
				else { // current overrides parent
					retValue = tmpValue
				}
			}
		}
		else {
			retValue = tmpValue // no parent
		}
		
		retValue
	}
	
}
