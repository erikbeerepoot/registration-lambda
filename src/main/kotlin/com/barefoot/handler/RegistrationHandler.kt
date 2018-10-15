package com.barefoot.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.barefoot.model.ApiGatewayResponse
import com.barefoot.model.HelloResponse
import com.barefoot.model.Registration
import com.barefoot.service.RegistrationService
import com.google.gson.Gson
import org.apache.logging.log4j.LogManager

class RegistrationHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
  private val logger = LogManager.getLogger(RegistrationHandler::class.java)
  private val gson = Gson()
  private val registrationService = RegistrationService()

  override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
    (input["httpMethod"] as String).let { method ->
      when (method) {
        "GET" -> {
          val queryParameters = input["queryStringParameters"] as? Map<*, *>
          logger.info("GET request. Query parameters: $queryParameters")
          val registration = queryParameters?.let { handleGET(it) }
          val code = if (registration != null) 200 else 404
          val text = if (registration != null) gson.toJson(registration) else "Registration not found"
          return ApiGatewayResponse.build {
            statusCode = code
            rawBody = text
          }
        }
        "POST" -> {
          val result = (input["body"] as String).let { handlePOST(it) }
          val code = if (result) 200 else 400
          val text = if (result) "Successfully saved registration." else "An error occurred while saving registration."
          return ApiGatewayResponse.build {
            statusCode = code
            rawBody = text
          }
        }
        else -> {
          logger.error("Unknown HTTP Method: $method")
          return ApiGatewayResponse.build {
            statusCode = 400
            rawBody = "Unknown HTTP method."
          }
        }
      }
    }
  }

  // Saves the registration
  private fun handlePOST(body: String): Boolean {
    val registration = gson.fromJson<Registration>(body, Registration::class.java)
    return registrationService.saveRegistration(registration)
  }

  // Fetches the registration
  private fun handleGET(queryStringParameters: Map<*, *>): Registration? {
    return (queryStringParameters["token"] as? String)?.let { deviceToken ->
      registrationService.getRegistration(deviceToken)
    }
  }
}