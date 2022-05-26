#pragma once


namespace java{
namespace lang{

class UnicodeScript: public Enum<UnicodeScript*>{
//fields
public:
    static std::vector<UnicodeScript*>* $VALUES;
    static UnicodeScript* ARABIC;
    static UnicodeScript* ARMENIAN;
    static UnicodeScript* AVESTAN;
    static UnicodeScript* BALINESE;
    static UnicodeScript* BAMUM;
    static UnicodeScript* BATAK;
    static UnicodeScript* BENGALI;
    static UnicodeScript* BOPOMOFO;
    static UnicodeScript* BRAHMI;
    static UnicodeScript* BRAILLE;
    static UnicodeScript* BUGINESE;
    static UnicodeScript* BUHID;
    static UnicodeScript* CANADIAN_ABORIGINAL;
    static UnicodeScript* CARIAN;
    static UnicodeScript* CHAKMA;
    static UnicodeScript* CHAM;
    static UnicodeScript* CHEROKEE;
    static UnicodeScript* COMMON;
    static UnicodeScript* COPTIC;
    static UnicodeScript* CUNEIFORM;
    static UnicodeScript* CYPRIOT;
    static UnicodeScript* CYRILLIC;
    static UnicodeScript* DESERET;
    static UnicodeScript* DEVANAGARI;
    static UnicodeScript* EGYPTIAN_HIEROGLYPHS;
    static UnicodeScript* ETHIOPIC;
    static UnicodeScript* GEORGIAN;
    static UnicodeScript* GLAGOLITIC;
    static UnicodeScript* GOTHIC;
    static UnicodeScript* GREEK;
    static UnicodeScript* GUJARATI;
    static UnicodeScript* GURMUKHI;
    static UnicodeScript* HAN;
    static UnicodeScript* HANGUL;
    static UnicodeScript* HANUNOO;
    static UnicodeScript* HEBREW;
    static UnicodeScript* HIRAGANA;
    static UnicodeScript* IMPERIAL_ARAMAIC;
    static UnicodeScript* INHERITED;
    static UnicodeScript* INSCRIPTIONAL_PAHLAVI;
    static UnicodeScript* INSCRIPTIONAL_PARTHIAN;
    static UnicodeScript* JAVANESE;
    static UnicodeScript* KAITHI;
    static UnicodeScript* KANNADA;
    static UnicodeScript* KATAKANA;
    static UnicodeScript* KAYAH_LI;
    static UnicodeScript* KHAROSHTHI;
    static UnicodeScript* KHMER;
    static UnicodeScript* LAO;
    static UnicodeScript* LATIN;
    static UnicodeScript* LEPCHA;
    static UnicodeScript* LIMBU;
    static UnicodeScript* LINEAR_B;
    static UnicodeScript* LISU;
    static UnicodeScript* LYCIAN;
    static UnicodeScript* LYDIAN;
    static UnicodeScript* MALAYALAM;
    static UnicodeScript* MANDAIC;
    static UnicodeScript* MEETEI_MAYEK;
    static UnicodeScript* MEROITIC_CURSIVE;
    static UnicodeScript* MEROITIC_HIEROGLYPHS;
    static UnicodeScript* MIAO;
    static UnicodeScript* MONGOLIAN;
    static UnicodeScript* MYANMAR;
    static UnicodeScript* NEW_TAI_LUE;
    static UnicodeScript* NKO;
    static UnicodeScript* OGHAM;
    static UnicodeScript* OLD_ITALIC;
    static UnicodeScript* OLD_PERSIAN;
    static UnicodeScript* OLD_SOUTH_ARABIAN;
    static UnicodeScript* OLD_TURKIC;
    static UnicodeScript* OL_CHIKI;
    static UnicodeScript* ORIYA;
    static UnicodeScript* OSMANYA;
    static UnicodeScript* PHAGS_PA;
    static UnicodeScript* PHOENICIAN;
    static UnicodeScript* REJANG;
    static UnicodeScript* RUNIC;
    static UnicodeScript* SAMARITAN;
    static UnicodeScript* SAURASHTRA;
    static UnicodeScript* SHARADA;
    static UnicodeScript* SHAVIAN;
    static UnicodeScript* SINHALA;
    static UnicodeScript* SORA_SOMPENG;
    static UnicodeScript* SUNDANESE;
    static UnicodeScript* SYLOTI_NAGRI;
    static UnicodeScript* SYRIAC;
    static UnicodeScript* TAGALOG;
    static UnicodeScript* TAGBANWA;
    static UnicodeScript* TAI_LE;
    static UnicodeScript* TAI_THAM;
    static UnicodeScript* TAI_VIET;
    static UnicodeScript* TAKRI;
    static UnicodeScript* TAMIL;
    static UnicodeScript* TELUGU;
    static UnicodeScript* THAANA;
    static UnicodeScript* THAI;
    static UnicodeScript* TIBETAN;
    static UnicodeScript* TIFINAGH;
    static UnicodeScript* UGARITIC;
    static UnicodeScript* UNKNOWN;
    static UnicodeScript* VAI;
    static UnicodeScript* YI;
    static java::util::HashMap<std::string*, UnicodeScript*>* aliases;
    static std::vector<int>* scriptStarts;
    static std::vector<UnicodeScript*>* scripts;

//methods
public:
    UnicodeScript();

    static UnicodeScript* forName(std::string* );

    static UnicodeScript* of(int );

    static UnicodeScript* valueOf(std::string* );

    static std::vector<UnicodeScript*>* values();


};//class UnicodeScript

}//namespace java
}//namespace lang
