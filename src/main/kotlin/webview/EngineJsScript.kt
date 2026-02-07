package com.score.webview

object EngineJsScript {
    var logScript = """
                      (function() {
                        console.log = function() {
                            var message = Array.prototype.slice.call(arguments).map(function(arg) {
                                return typeof arg === 'object' ? JSON.stringify(arg) : String(arg);
                            }).join(' ');

                            if (window.javaBridge) {
                                window.javaBridge.printLog('[LOG] ' + message);
                            }
                        };
                        
                        console.warn = function() {
                            var message = Array.prototype.slice.call(arguments).map(function(arg) {
                                return typeof arg === 'object' ? JSON.stringify(arg) : String(arg);
                            }).join(' ');

                            if (window.javaBridge) {
                                window.javaBridge.printLog('[LOG] ' + message);
                            }
                        };
                        
                        console.error = function() {
                            var message = Array.prototype.slice.call(arguments).map(function(arg) {
                                return typeof arg === 'object' ? JSON.stringify(arg) : String(arg);
                            }).join(' ');

                            if (window.javaBridge) {
                                window.javaBridge.printLog('[LOG] ' + message);
                            }
                        };

                        window.onerror = function(msg, url, line, col, error) {
                            var errorMessage = 'ERROR: ' + msg + ' at ' + url + ':' + line + ':' + col;
                            if (error && error.stack) {
                                errorMessage += '\nStack: ' + error.stack;
                            }
                            if (window.javaBridge) {
                                window.javaBridge.printLog(errorMessage);
                            }
                            return false;
                        };
                      
                        // Vue 에러 핸들러 등록을 위한 헬퍼 함수
                        window.setupVueErrorHandler = function(app) {
                            if (app && app.config) {
                                app.config.errorHandler = function(err, instance, info) {
                                    var errorMessage = 'VUE ERROR: ' + err.toString() + '\nInfo: ' + info;
                                    if (err.stack) {
                                        errorMessage += '\nStack: ' + err.stack;
                                    }
                                    if (window.javaBridge) {
                                        window.javaBridge.printLog(errorMessage);
                                    }
                                    console.error('Vue Error:', err, info);
                                };
                                
                                app.config.warnHandler = function(msg, instance, trace) {
                                    var warnMessage = 'VUE WARNING: ' + msg + '\nTrace: ' + trace;
                                    if (window.javaBridge) {
                                        window.javaBridge.printLog(warnMessage);
                                    }
                                };
                            }
                        };
                      
                        window.dispatchEvent(new CustomEvent('javaReady'));
                      })();
                    """.trimIndent()
}