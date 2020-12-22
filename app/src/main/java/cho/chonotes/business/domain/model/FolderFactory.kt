package cho.chonotes.business.domain.model

import cho.chonotes.business.domain.util.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderFactory
@Inject
constructor(
    private val dateUtil: DateUtil
) {

    fun createSingleFolder(
        folder_id: String? = null,
        folder_name: String,
        notes_count: Int? = null,
        uid: String
    ): Folder {
        return Folder(
            folder_id = folder_id ?: UUID.randomUUID().toString(),
            folder_name = folder_name,
            notes_count = notes_count ?: 0,
            uid = uid,
            created_at = dateUtil.getCurrentTimestamp(),
            updated_at = dateUtil.getCurrentTimestamp()
        )
    }

    fun createDefaultFolder(
        uid: String
    ): Folder {
        return Folder(
            folder_id = uid,
            folder_name = "notes",
            notes_count = 0,
            uid = uid,
            created_at = dateUtil.getCurrentTimestamp(),
            updated_at = dateUtil.getCurrentTimestamp()
        )
    }

}
