package com.example.blog

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BlogConfiguration {
    @Bean
    fun databaseInitializer(
        userRepository: UserRepository,
        articleRepository: ArticleRepository,
        publicationsRepository: PublicationsRepository
    ) = ApplicationRunner {
        val johnDoe = userRepository.save(
            User(
                username = "johnDoe",
                firstName = "Dr. John",
                lastName = "Doe",
                designation = "faculty"
            )
        )
        articleRepository.save(
            Article(
                title = "Why",
                headline = "Nuts",
                content = "Deez Nuts are good",
                author = johnDoe
            )
        )
        articleRepository.save(
            Article(
                title = "Nuts",
                headline = "Gay",
                content = "Every nut is a good nut",
                author = johnDoe
            )
        )
        val publication1 = Publication(title = "Lol", journal = "Nuts", author = johnDoe)
        johnDoe.publications.add(publication1)
        publicationsRepository.save(publication1)
        userRepository.save(
            User(
                username = "bagesh_kumar",
                firstName = "Bagesh",
                lastName = "Kumar",
                designation = "researcher",
                description = "Integrated MTech + PhD @ IIIT Allahabad. Works in ML and NLP.",
                address = "CC3, IIIT Allahabad",
                imageUrl = "https://serl.iiita.ac.in/Profile/bagesh.jpg"
            )
        )
    }
}
