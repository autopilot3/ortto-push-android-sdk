import './style.css';
import { Ap3cWebView } from './webview';

const ap3cWebView = window.ap3cWebView = new Ap3cWebView();

if (ap3cWebView.hasConfig()) {
    ap3cWebView.start();
}

