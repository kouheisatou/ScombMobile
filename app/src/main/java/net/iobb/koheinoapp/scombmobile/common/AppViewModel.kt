package net.iobb.koheinoapp.scombmobile.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

const val SCOMB_LOGIN_PAGE_URL = "https://scombz.shibaura-it.ac.jp/saml/login?idp=http://adfs.sic.shibaura-it.ac.jp/adfs/services/trust"
const val SCOMB_HOME_URL = "https://scombz.shibaura-it.ac.jp/portal/home"
const val SCOMB_LOGGED_OUT_PAGE_URL = "https://scombz.shibaura-it.ac.jp/login"
const val SCOMB_TIMETABLE_URL = "https://scombz.shibaura-it.ac.jp/lms/timetable"
const val CLASS_PAGE_URL = "https://scombz.shibaura-it.ac.jp/lms/course?idnumber="
const val TASK_LIST_PAGE_URL = "https://scombz.shibaura-it.ac.jp/lms/task"

/** ScombのCookieとして保存されているセッションIDのキー **/
const val SESSION_COOKIE_ID = "SESSION"

/** 時間割をローカルに保存する有効期限 **/
const val TIMETABLE_EFFECTIVE_TIME = 24

// Scombに2段階認証が未設定でログインしようとすると出る"2要素認証は無効になっています。"確認画面の"次へ"ボタンのhtmlのid
const val TWO_STEP_VERIFICATION_LOGIN_BUTTON_ID = "continueButton"

/** サイトのヘッダー要素ID **/
const val HEADER_ELEMENT_ID = "page_head"
/** サイトのフッター要素ID **/
const val FOOTER_ELEMENT_ID = "page_foot"

// --------時間割CSS----------
/** 時間割のテーブル1行のCSSクラス名 **/
const val TIMETABLE_ROW_CSS_CLASS_NM = "div-table-data-row"
/** 時間割の1マスのCSSクラス名 **/
const val TIMETABLE_CELL_CSS_CLASS_NM = "div-table-cell"
/** 時間割のマス内のトップボタンCSSクラス名 **/
const val TIMETABLE_CELL_HEADER_CSS_CLASS_NM = "timetable-course-top-btn"
/** 時間割のマス内の詳細情報CSSクラス名 **/
const val TIMETABLE_CELL_DETAIL_CSS_CLASS_NM = "div-table-cell-detail"
/** 教室名のattributeキー **/
const val TIMETABLE_ROOM_ATTR_KEY = "title"

// --------課題一覧ページCSS---------
/** 課題1行分のCSS **/
const val TASK_LIST_CSS_CLASS_NM = "result_list_line"
/** 課題行の科目名列のCSS **/
const val TASK_LIST_CLASS_CULUMN_CSS_NM = "tasklist-course"
/** 課題行の課題タイプ列のCSS **/
const val TASK_LIST_TYPE_CULUMN_CSS_NM = "tasklist-contents"
/** 課題行の課題タイトル列のCSS **/
const val TASK_LIST_TITLE_CULUMN_CSS_NM = "tasklist-title"
/** 課題行の締切列のCSS **/
const val TASK_LIST_DEADLINE_CULUMN_CSS_NM = "tasklist-deadline"

class AppViewModel : ViewModel(){

    var sessionId: String? = null
    var userId = MutableLiveData("")
}