package com.example.blog

import org.springframework.data.repository.CrudRepository

interface ArticleRepository : CrudRepository<Article, Long> {
    fun findBySlug(slug: String): Article?
    fun findAllByOrderByAddedAtDesc(): Iterable<Article>
}
interface UserRepository : CrudRepository<User, Long> {
    fun findByUsername(login: String): User?
    fun findAllByDesignation(designation: String): Iterable<User>
}

interface PublicationsRepository : CrudRepository<Publication, Long> {
    fun findAllByAuthorUsername(authorFirstName: String): Iterable<Publication>
}

interface SubscriptionRepository : CrudRepository<Subscription, Long>

interface FeedbackRepository : CrudRepository<Feedback, Long>