import type { Ap3c, Ap3cWebViewConfig, WidgetData } from './types';
import { Ap3cWebView } from './webview';

declare global {
    interface Window {
        ap3c: Ap3c;

        ap3cWebView: Ap3cWebView;

        Android?: {
            showMessage(jsonString: string): void;
        };

        webkit?: {
            messageHandlers?: {
                messageHandler?: {
                    postMessage<T extends Record<string, any>>(message: T): void;
                }
            };
        };
    }
}

