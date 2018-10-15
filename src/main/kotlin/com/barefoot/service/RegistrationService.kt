package com.barefoot.service

import com.barefoot.model.Registration
import redis.clients.jedis.Jedis

class RegistrationService {
  val jedis = Jedis("reg-se-lghdpq2t6b1p.0tiju7.0001.usw2.cache.amazonaws.com")

  fun getRegistration(deviceToken: String): Registration? {
    val companies = jedis.use { jedisClient ->
      jedisClient.smembers(deviceToken).map { it.toInt() }
    }
    return if (companies.isEmpty()) {
      null
    } else {
      Registration(deviceToken, companies)
    }
  }

  fun saveRegistration(registration: Registration): Boolean {
    return jedis.use { jedisClient ->
      val companies = registration.companies.map { it.toString() }.toTypedArray()
      jedisClient.del(registration.token)
      val addedCount = jedisClient.sadd(registration.token, *companies)
      addedCount == companies.count().toLong()
    }
  }
}