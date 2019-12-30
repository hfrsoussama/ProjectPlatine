package com.hfrsoussama.projectplatine.feat.posts.core.network

import com.hfrsoussama.projectplatine.shared.database.dao.CommentDao
import com.hfrsoussama.projectplatine.shared.database.entities.CommentDb
import com.hfrsoussama.projectplatine.shared.database.entities.PostDb
import com.hfrsoussama.projectplatine.shared.database.entities.PostWithCommentsDb
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test


class CommentRetrievalStrategyTest {

    private val remoteClient = mockk<PostsWebServices>()
    private val commentDao = mockk<CommentDao>()

    private val localOnlyRetrievalStrategy = LocalOnlyCommentRetrievalStrategy()

    @Before
    fun generateStubs() {
        coEvery {
            commentDao.getAllCommentsDbForPost(any())
        } answers {
            listOf(
                PostWithCommentsDb(
                    postDb = PostDb(arg(0), 1, "The post title", "the post body"),
                    commentsDbList = listOf(
                        CommentDb(1, 1, "first comment title", "b@b.com", "first comment body"),
                        CommentDb(2, 1, "second comment title", "b@b.com", "second comment body")
                    )
                )
            )
        }

        coEvery {
            commentDao.getAllCommentsDb()
        } returns listOf(
            CommentDb(1, 1, "first comment title", "b@b.com", "first comment body"),
            CommentDb(2, 1, "second comment title", "b@b.com", "second comment body")
        )


        coEvery {
            remoteClient.getCommentsByPostId(any())
        } returns listOf()
    }


    @Test
    fun localStrategyShouldNeverCallAWebService() = runBlocking {
        localOnlyRetrievalStrategy.retrieveComments(remoteClient, commentDao)
        verify {
            remoteClient wasNot Called
        }
    }

    @Test
    fun localStrategyShouldUseOnceTheDatabase() = runBlocking {
        localOnlyRetrievalStrategy.retrieveComments(remoteClient, commentDao)
        coVerify(exactly = 1) {
            commentDao.getAllCommentsDb()
        }
    }

}