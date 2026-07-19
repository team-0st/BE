package com.zerost.api.soup.domain

import org.springframework.data.jpa.repository.JpaRepository

interface SoupRepository : JpaRepository<Soup, Long>
