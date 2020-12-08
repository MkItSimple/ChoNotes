package cho.chonotes.framework.datasource.preferences

class PreferenceKeys {

    companion object{

        // Shared Preference Files:
        const val NOTE_PREFERENCES: String = "cho.chonotes.notes"

        // Shared Preference Keys
        val NOTE_FILTER: String = "${NOTE_PREFERENCES}.NOTE_FILTER"
        val NOTE_ORDER: String = "${NOTE_PREFERENCES}.NOTE_ORDER"

        // Shared Preference Files:
        const val FOLDER_PREFERENCES: String = "com.example.choplaygroundkotlin.notes"

        // Shared Preference Keys
        val FOLDER_FILTER: String = "${FOLDER_PREFERENCES}.FOLDER_FILTER"
        val FOLDER_ORDER: String = "${FOLDER_PREFERENCES}.FOLDER_ORDER"

    }
}