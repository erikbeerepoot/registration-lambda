package com.barefoot.model

data class HelloResponse(val message: String, val input: Map<String, Any>) : Response()
