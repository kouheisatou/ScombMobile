package net.iobb.koheinoapp.scombmobile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

const val SCOMB_LOGIN_PAGE_URL = "https://scombz.shibaura-it.ac.jp/saml/login?idp=http://adfs.sic.shibaura-it.ac.jp/adfs/services/trust"
const val SCOMB_HOME_URL = "https://scombz.shibaura-it.ac.jp/portal/home"
const val SCOMB_LOGGED_OUT_PAGE_URL = "https://scombz.shibaura-it.ac.jp/login"
const val SCOMB_TIMETABLE_URL = "https://scombz.shibaura-it.ac.jp/lms/timetable"

// Scombに2段階認証が未設定でログインしようとすると出る"2要素認証は無効になっています。"確認画面の"次へ"ボタンのhtmlのid
const val TWO_STEP_VERIFICATION_LOGIN_BUTTON_ID = "continueButton"

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

class AppViewModel : ViewModel(){

    var sessionId: String? = null
    var userId = MutableLiveData("")
}