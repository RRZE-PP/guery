package de.rrze.guery.base

import de.rrze.guery.operator.DelegatingClosureOperationManager
import de.rrze.guery.operator.IOperationManager
import de.rrze.guery.operator.Operator
import org.slf4j.LoggerFactory

class DelegatingQueryBase extends QueryBase {

    static log = LoggerFactory.getLogger(DelegatingQueryBase.class)

	final QueryBase parent
	final IOperationManager operationManager = new DelegatingClosureOperationManager()
	
	
	
	def DelegatingQueryBase() {
		super()
	}
	
	def DelegatingQueryBase(QueryBase parentQb) {
		super()
		parent = parentQb
		operationManager.parent = parentQb.operationManager
	}
	
	
	Map<String,Operator> getOperators() {
		getMergedFieldValue('operators')
	}
	
	Map<String,Filter> getFilters() {
		getMergedFieldValue('filters')
	}
	
	Map<String,String> getLang() {
		getMergedFieldValue('lang')
	}
	
	Boolean getSortable() {
		getMergedFieldValue('sortable')
	}
	
	Set<String> getPlugins() {
		getMergedFieldValue('plugins')
	}
	
	Boolean getAllowEmpty() {
		getMergedFieldValue('allowEmpty')
	}
	
	List<String> getConditions() {
		getMergedFieldValue('conditions')
	}
	
	String getDefaultCondition() {
		getMergedFieldValue('defaultCondition')
	}
	
	
	
	def getMergedFieldValue(String fieldName) {
		def retValue
		def localValue = this."_${fieldName}"
//		if (fieldName in ['_sortable']) log.warn("${fieldName} --> ${tmpValue}")
		
		if (parent) {
			if (localValue == null) { // if there is a parent and the stored value is empty
				retValue = parent."${fieldName}" // delegate to parent
			}
			else { // if there is a parent and the stored value is NOT empty
				
				// MAGIC MERGE FILTERS BY LABEL // TODO maybe needs some more checks
//				if (fieldName == 'filters') {
//					def parentValue = parent."${fieldName}"
//					def filterSet = parentValue.values() + localValue.values()
//					def flatFiltersByLabel = filterSet.groupBy { it.label?:it.id } // use id, if no label is specified
//					def collapsedFlatFilters = flatFiltersByLabel.collect { k, v ->
//						def cf = v.first()
//						if (v.size() > 1) {
//							v.each { ff ->
//								cf.operators.addAll(ff.operators)
//							}
//							cf.operators = cf.operators.unique()
//						}
//						return cf
//					}
//					retValue = collapsedFlatFilters.collectEntries { [it.id, it] }
//				}
				
				// MAGIC MERGE FILTERS BY ID // TODO maybe needs some more checks
				if (fieldName == 'filters') {
					def parentValue = parent."${fieldName}"
					def filterSet = parentValue.values() + localValue.values()
					def flatFiltersById = filterSet.groupBy { it.id } // use id, if no label is specified
					def collapsedFlatFilters = flatFiltersById.collect { k, v ->
						def cf = v.first()
						if (v.size() > 1) {
							v.each { ff ->
								cf.operators.addAll(ff.operators)
							}
							cf.operators = cf.operators.unique()
						}
						return cf
					}
					retValue = collapsedFlatFilters.collectEntries { [it.id, it] }
				}

				// MERGE LANG
				if (fieldName == 'lang') {
					retValue = parent.lang
					retValue.operators.putAll(localValue.operators)
					retValue.errors.putAll(localValue.errors)
					retValue.conditions.putAll(localValue.conditions)
				}
								
				// GENERIC MERGE
				if (!retValue) {
					if (localValue in Map) { // merge with parent map
						retValue = [:]
						def parentValue = parent."${fieldName}"
						if (parentValue) retValue.putAll(parentValue)
						retValue.putAll(localValue) // current overrides parent
					}
					else if (localValue in Collection) { // merge with parent collection
						retValue = []
						def parentValue = parent."${fieldName}"
						if (parentValue) retValue.addAll(parentValue)
						retValue.addAll(localValue)
					}
					else { // current overrides parent
						retValue = localValue
					}
				}
				
				
			}
		}
		else {
			retValue = localValue // no parent
		}
		
		retValue
	}
	
}
