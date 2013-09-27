package com.chrisstucchio.tiramisu

trait Syntax {
  implicit class StringToQuery(s: String) {
    def sql: BaseQuery = new BaseQuery(s, Seq())
    def sqlP(params: SqlParameter[_]*): BaseQuery = new BaseQuery(s, params)
  }

  implicit class ValToParam(v: SqlParameter[_]) {
    def sqlV: BaseQuery = BaseQuery(" ? ", Seq(v))
  }
}
