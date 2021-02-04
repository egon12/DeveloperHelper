package com.egon12.developerhelper.rest

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionTreeNodeTest {

    @Test
    fun testTree() {

        val colItem = Collection.Item.Folder("name", emptyList())
        val targetItem = Collection.Item.Folder("target", emptyList())
        val root = CollectionTreeNode(
            colItem, 0, listOf(
                CollectionTreeNode(colItem, 1, emptyList()),
                CollectionTreeNode(
                    colItem, 1, listOf(
                        CollectionTreeNode(targetItem, 2, emptyList())
                    )
                ),
                CollectionTreeNode(
                    colItem, 1, listOf(
                        CollectionTreeNode(targetItem, 2, emptyList())
                    )
                )
            )
        )

        root.expandToLevel(3)

        assertEquals(6, root.count())
        assertEquals(targetItem, root.get(3)!!.item)

        root.get(2)!!.collapse()

        assertEquals(5, root.count())
        assertEquals(colItem, root.get(3)!!.item)


    }
}
