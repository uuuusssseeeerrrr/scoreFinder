package com.score.webview

import com.prof18.rssparser.RssParser

object GitReleaseParser {
    private const val GITHUB_RSS_URL = "https://github.com/uuuusssseeeerrrr/scoreFinder/releases.atom"
    private var _currentVersion = ""

    var currentVersion: String
        get() = _currentVersion
        set(value) {
            _currentVersion = value
        }

    suspend fun parse() {
        val rssParser = RssParser()
        val rssChannel = rssParser.getRssChannel(GITHUB_RSS_URL)
        currentVersion = rssChannel.items[0].title ?: ""
    }
}