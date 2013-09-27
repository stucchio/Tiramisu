package com.chrisstucchio.tiramisu

trait Syntax {
  implicit class StringToQuery(s: String) {
    def sql: Query = Query(s, Seq())
    def sqlP(params: SqlParameter[_]*): Query = Query(s, params)
  }
}
