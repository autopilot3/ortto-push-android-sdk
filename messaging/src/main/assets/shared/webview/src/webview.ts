import memoize from '@stdlib/utils/memoize';
import { Widget, Ap3cWebViewConfig } from './types';

export class Ap3cWebView {
    public loaded: boolean = false;

    private config?: Ap3cWebViewConfig = undefined;

    setConfig = (config: Ap3cWebViewConfig) => {
        this.config = config;
    }

    getConfig = () => this.config;

    hasConfig = () => this.config != null;

    runWidget = (widget: Widget): void => {
        window.ap3c.widgetsFuncs[widget.type].inject(widget);
        window.ap3c._generalWidget.show(widget);
    }

    sendMessageToNative = <T extends Record<string, any>>(data: T): void => {
        try {
            // Check if the Android object exists
            if (window.Android) {
                window.Android.showMessage(JSON.stringify(data));
            }
            // Check if the iOS WKWebView handlers exist
            else if (window.webkit?.messageHandlers?.messageHandler != null) {
                window.webkit.messageHandlers.messageHandler.postMessage(data);
            } else {
                console.log('Native code interface is not available');
            }
        } catch (err) {
            console.log('Error sending message to native code: ' + err);
        }
    }

    initWindow = () => {
        window.ap3c = window.ap3c || {};
        window.ap3c.cmd = window.ap3c.cmd || [];
    };

    initAp3c = () => {
        window.ap3c.activity = function(act) { window.ap3c.act = (window.ap3c.act || []); window.ap3c.act.push(act); };

        const scriptElement = document.createElement('script');
        scriptElement.type = 'text/javascript';
        scriptElement.src = this.config!.captureJsUrl;
        scriptElement.async = true;
        scriptElement.defer = true;

        document.body.appendChild(scriptElement);
    };

    pushAp3cCommands = (...commands: ((this: Ap3cWebView) => void)[]) => {
        window.ap3c.cmd.push(...commands);
    };

    shimAp3c = () => {
        window.ap3c.getWidgetsData = memoize(() => {
            let data = this.config?.data!;
            data.widgets = data.widgets
                .filter(w => w.type === 'popup')
                .map(w => ({
                    ...w,
                    trigger: {
                        ...w.trigger,
                        selection: 'load',
                    },
                    when: {
                        ...w.when,
                        selection: 'load',
                    },
                }));

            return JSON.stringify(data);
        }) as typeof window.ap3c.getWidgetsData;

        const originalHide = window.ap3c._generalWidget.hide;

        window.ap3c._generalWidget.hide = (widget, template) => {
            originalHide(widget, template);

            window.ap3cWebView.sendMessageToNative({
                type: 'widget-close',
                id: widget.id,
            });
        };

        const originalTrack = window.ap3c.track;
        window.ap3c.track = (options, callback) => {
            originalTrack(options, callback);

            window.ap3cWebView.sendMessageToNative({
                type: 'ap3c-track',
                payload: options,
            });
        };

        window.ap3c._generalWidget.canExecute = () => true;
    };

    start = () => {
        if (!this.config) {
            throw new Error("No config provided");
        }

        const { token, data, endpoint } = this.config;

        this.initWindow();
        this.pushAp3cCommands(
            this.shimAp3c,
            () => window.ap3c.init(token, endpoint),
            () => {
                window.ap3c.enableTalk = false;
                this.runWidget(data.widgets[0]);
            },
            () => {
                console.log("Ap3c loaded");
                this.loaded = true;
            },
        );
        this.initAp3c();
    }
}
