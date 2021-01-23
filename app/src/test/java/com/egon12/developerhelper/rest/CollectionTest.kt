package com.egon12.developerhelper.rest

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionTest {

    @Test
    fun testParseCollectionSimple() {
        val str = """
            {
                "info": {
                    "_postman_id": "12345",
                    "name": "Somekind of something V2",
                    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
                },
                "item": [ ]
			}
			"""

        val col = Collection.parse(str)
        assertEquals(0, col.item.size)
    }


    @Test
    fun testParseCollectionItemRequest() {
        val str = """
            {
                "info": {
                    "_postman_id": "12345",
                    "name": "Somekind of something V2",
                    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
                },
                "item": [ {"name": "what", "request":{"method":"GET","url":{
                    "raw":"{{host}}/v1/books",
                    "host": ["{{host}}"],
                    "path": ["v1", "books"]
                }}} ]
			}
			"""

        val col = Collection.parse(str)
        assert(col.item[0] is Collection.Item.RequestItem)
    }

    @Test
    fun testParseCollectionItemFolder() {
        val str = """
            {
                "info": {
                    "_postman_id": "12345",
                    "name": "Somekind of something V2",
                    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
                },
                "item": [ {"name": "what", "item":[
                    {"name":"kucing","item":[]}, 
                    {"name":"dua","item":[]} 
                ]}]
			}
			"""

        val col = Collection.parse(str)
        assert(col.item[0] is Collection.Item.Folder)

        val item0 = col.item[0] as Collection.Item.Folder
        assertEquals("kucing", (item0.item[0] as Collection.Item.Folder).name)
        assertEquals("dua", (item0.item[1] as Collection.Item.Folder).name)
    }


    @Test
    fun testParseCollection() {
        val str = """
            {
                "info": {
                    "_postman_id": "12345",
                    "name": "Somekind of something V2",
                    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
                },
                "item": [
                    {
                        "name": "Group1",
                        "item": [
                            {
                                "name": "Group2",
                                "item": [{
                                    "name":"Request1",
                                    "request": {
										"method": "GET",
										"header": [
											{
												"key": "Accept",
												"value": "*"
											},
											{
												"key": "Authorization",
												"value": "Bearer {{intools-token}}"
											},
											{
												"key": "Referer",
												"value": "https://www.tokopedia.com",
												"type": "text"
											}
										],
										"url": {
											"raw": "{{host}}/v1/something?query1=1&query2=2",
											"host": [
												"{{host}}"
											],
											"path": [
												"v1",
												"something",
												"section"
											],
											"query": [
												{
													"key": "query1",
													"value": "1"
												},
												{
													"key": "query2",
													"value": "2"
												}
											]
										},
                                        "body": {
                                            "mode": "raw",
                                            "raw": "{\"hello\":\"world\"}"
                                        }
                                    }
                                }]
                            }
                        ]
                    }]
                }
        """.trimIndent()

        val col: Collection = Collection.parse(str)
        println(col.encode())
    }
}