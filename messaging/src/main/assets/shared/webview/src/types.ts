export interface Widget {
    id: string;
    type: string;
    trigger: {
        selection: string;
    };
    when: {
        selection: string;
    };
}

export interface WidgetData {
    widgets: Widget[];
}

export interface Ap3c {
    enableTalk: boolean;
    init(token: string, endpoint: string): void;
    cmd: (() => void)[];
    getCookie(): string|null;
    getWidgetsData(): string;
    _generalWidget: {
        show(widget: Widget, template?: string, timeoutSeconds?: number): void;
        hide(widget: Widget, template?: string): void;
        canExecute(widget: Widget, state: unknown): boolean;
    };
    act: unknown[];
    activity(act: unknown): void;
    widgetsFuncs: Record<string, {
        inject(widget: Widget, hasLogo?: boolean): void;
    }>
    track(options: Record<string, any>, callback: () => void): void;
}

export interface Ap3cWebViewConfig {
    token: string;
    endpoint: string;
    captureJsUrl: string;
    data: WidgetData;
}
