package org.grails.plugin.guery.util

class IntersectUtil {

    /**
     * Classic method
     * @param acc
     * @param opRes
     * @return
     */
    static Collection intersect(Collection acc, Collection opRes) {
        def missing = acc.findAll { accit -> !(opRes.find { accit.is(it) }) }
        acc.removeAll(missing) // intersect
        acc
    }

    /**
     * Faster method by unrz157
     *
     * This method is a bit weaker then the classic intersect because it does not really check
     * object identity (a.is(b)) but uses object equality (a.equals(b))
     *
     * @param acc
     * @param opRes
     * @return
     */
    static Collection intersectWithEqualityMap(Collection acc, Collection opRes) {
        def grp = opRes.groupBy { it -> it }
        def acc2 = []
        acc.each { a ->
            if (grp.containsKey(a)) {
                acc2.add(a)
            }
        }
        acc2
    }

    /**
     * Faster method based on method by unrz157
     *
     * This methods uses a HashMap to archieve O(1) lookup performance but uses an IdentityHashMap to
     * maintain the check for object identity at the same time.
     *
     * @param acc
     * @param opRes
     * @return
     */
    static Collection intersectWithIdentityMap(Collection acc, Collection opRes) {
        def grp = new IdentityHashMap(opRes.size())
        opRes.each { grp.put(it,null) }
        def acc2 = []
        acc.each { a ->
            if (grp.containsKey(a)) {
                acc2.add(a)
            }
        }
        acc2
    }


}
