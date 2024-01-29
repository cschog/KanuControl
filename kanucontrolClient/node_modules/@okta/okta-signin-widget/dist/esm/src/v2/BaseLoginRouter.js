import { Router, loc } from '../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import Backbone from '../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Settings from '../models/Settings.js';
import Bundles from '../util/Bundles.js';
import fn$2 from '../util/BrowserFeatures.js';
import fn from '../util/ColorsUtil.js';
import Enums from '../util/Enums.js';
import { ConfigError } from '../util/Errors.js';
import Logger from '../util/Logger.js';
import LanguageUtil from '../util/LanguageUtil.js';
import AuthContainer from '../v1/views/shared/AuthContainer.js';
import Header from '../v1/views/shared/Header.js';
import AppState from './models/AppState.js';
import sessionStorageHelper from './client/sessionStorageHelper.js';
import '@okta/okta-auth-js';
import { startLoginFlow } from './client/startLoginFlow.js';
import { handleConfiguredFlow } from './client/handleConfiguredFlow.js';
import { updateAppState } from './client/updateAppState.js';
import fn$1 from '../util/CookieUtil.js';
import { formatError } from './client/formatError.js';
import IonResponseHelper from './ion/IonResponseHelper.js';

/* eslint max-depth: [1,4] */

class BaseLoginRouter extends Router {
  // also set on prototype
  constructor(options) {
    super(options); // Create a default success and/or error handler if
    // one is not provided.

    this.Events = Backbone.Events;
    this.hasControllerRendered = false;
    this.settings = void 0;
    this.appState = void 0;
    this.hooks = void 0;
    this.header = void 0;

    if (!options.globalSuccessFn) {
      options.globalSuccessFn = function () {
        /* dummy function */
      };
    }

    if (!options.globalErrorFn) {
      options.globalErrorFn = function (err) {
        Logger.error(err);
      };
    }

    this.settings = new Settings(oktaUnderscore.omit(options, 'el', 'authClient', 'hooks'), {
      parse: true
    });
    this.settings.setAuthClient(options.authClient);

    if (!options.el) {
      this.settings.callGlobalError(new ConfigError(loc('error.required.el')));
    }

    oktaJQueryStatic('body > div').on('click', function () {// OKTA-69769 Tooltip wont close on iPhone/iPad
      // Registering a click handler on the first div
      // allows a tap that falls outside the tooltip
      // to be registered as a tap by iOS
      // and then the open tooltip will lose focus and close.
    });
    this.hooks = options.hooks;
    this.appState = new AppState({}, {
      settings: this.settings,
      hooks: this.hooks
    });
    const wrapper = new AuthContainer({
      appState: this.appState
    });
    oktaJQueryStatic(options.el).append(wrapper.render().$el);
    this.el = `#${Enums.WIDGET_CONTAINER_ID}`;
    this.header = new Header({
      el: this.el,
      appState: this.appState,
      settings: this.settings
    }); // Hide until unitial render

    this.hide();
    this.listenTo(this.appState, 'change:deviceFingerprint', this.updateDeviceFingerprint);
    this.listenTo(this.appState, 'restartLoginFlow', this.restartLoginFlow);
  }

  updateDeviceFingerprint() {
    const authClient = this.settings.getAuthClient();
    const fingerprint = this.appState.get('deviceFingerprint');

    if (fingerprint) {
      authClient.http.setRequestHeader('X-Device-Fingerprint', fingerprint);
    }
  }

  async handleIdxResponseFailure(error = {
    error: 'unknown',
    details: undefined
  }) {
    // IDX errors will not call the global error handler
    error = formatError(error);
    await updateAppState(this.appState, error.details);
  } // Generic error handler for all exceptions


  async handleError(error = {
    error: 'unknown',
    details: undefined
  }) {
    const formattedError = formatError({ ...error
    }); // format the error to resemble an IDX response

    await updateAppState(this.appState, formattedError.details);
  }
  /* eslint max-statements: [2, 36], complexity: [2, 16] */


  async render(Controller, options = {}) {
    // If url changes then widget assumes that user's intention was to initiate a new login flow,
    // so clear stored token to use the latest token.
    if (sessionStorageHelper.getLastInitiatedLoginUrl() !== window.location.href) {
      sessionStorageHelper.removeStateHandle();
    } // Since we have a wrapper view, render our wrapper and use its content
    // element as our new el.
    // Note: Render it here because we know dom is ready at this point


    if (!this.header.rendered()) {
      this.el = this.header.render().getContentEl();
    } // If we need to load a language (or apply custom i18n overrides), do
    // this now and re-run render after it's finished.


    if (!Bundles.isLoaded(this.settings.get('languageCode'))) {
      await LanguageUtil.loadLanguage(this.appState, this.settings);
    } // introspect stateToken when widget is bootstrap with state token
    // and remove it from `settings` afterwards as IDX response always has
    // state token (which will be set into AppState)


    let error;

    if (this.settings.get('oieEnabled')) {
      try {
        let idxResp = await startLoginFlow(this.settings);

        if (idxResp.error) {
          await this.handleIdxResponseFailure(idxResp.error);
        } else {
          if (this.settings.get('flow') && !this.hasControllerRendered) {
            idxResp = await handleConfiguredFlow(idxResp, this.settings);
          } // TODO: OKTA-494979 - temporary fix, remove when auth-js is upgraded to 6.6+


          if (!idxResp.requestDidSucceed && IonResponseHelper.isIdxSessionExpiredError(idxResp)) {
            // clear transaction subsequent page loads do not use stale interactionHandle
            const authClient = this.settings.getAuthClient();
            authClient.transactionManager.clear();
          }

          await updateAppState(this.appState, idxResp);
        }
      } catch (exception) {
        var _exception$is;

        if ((_exception$is = exception.is) !== null && _exception$is !== void 0 && _exception$is.call(exception, 'terminal')) {
          this.appState.setNonIdxError(exception);
        } else {
          error = exception;
          await this.handleError(exception);
        }
      } finally {
        // These settings should only be used one time, for initial render
        this.settings.unset('stateToken');
        this.settings.unset('proxyIdxResponse');
      }
    } // Load the custom colors only on the first render


    if (this.settings.get('colors.brand') && !fn.isLoaded()) {
      const colors = {
        brand: this.settings.get('colors.brand')
      };
      fn.addStyle(colors);
    } // Show before initial render


    this.show(); // render Controller

    this.unload();

    const controllerOptions = oktaUnderscore.extend({
      el: this.el,
      settings: this.settings,
      appState: this.appState
    }, options);

    this.controller = new Controller(controllerOptions); // Bubble up all controller events

    this.listenTo(this.controller, 'all', this.trigger);
    this.controller.render();
    this.hasControllerRendered = true; // This will reject the promise returned from renderEl

    if (error) {
      this.settings.callGlobalError(error);
    } // -- TODO: OKTA-244631 How to surface up the CORS error in IDX?
    // -- The `err` object from idx.js doesn't have XHR object
    // Global error handling for CORS enabled errors
    // if (err.xhr && BrowserFeatures.corsIsNotEnabled(err.xhr)) {
    //   this.settings.callGlobalError(new UnsupportedBrowserError(loc('error.enabled.cors')));
    //   return;
    // }

  }
  /**
    * When "Remember My Username" is enabled, we save the identifier in a cookie
    * so that the next time the user visits the SIW, the identifier field can be 
    * pre-filled with this value.
   */


  updateIdentifierCookie(idxResponse) {
    if (this.settings.get('features.rememberMe')) {
      var _idxResponse$context;

      // Update the cookie with the identifier
      const user = idxResponse === null || idxResponse === void 0 ? void 0 : (_idxResponse$context = idxResponse.context) === null || _idxResponse$context === void 0 ? void 0 : _idxResponse$context.user;
      const {
        identifier: identifier
      } = (user === null || user === void 0 ? void 0 : user.value) || {};

      if (identifier) {
        fn$1.setUsernameCookie(identifier);
      }
    } else {
      // We remove the cookie explicitly if this feature is disabled.
      fn$1.removeUsernameCookie();
    }
  }

  hasAuthenticationSucceeded(idxResponse) {
    var _idxResponse$rawIdxSt, _idxResponse$rawIdxSt2;

    // Check whether authentication has succeeded. This is done by checking the server response
    // and seeing if either the 'success' or 'successWithInteractionCode' objects are present.
    return (idxResponse === null || idxResponse === void 0 ? void 0 : (_idxResponse$rawIdxSt = idxResponse.rawIdxState) === null || _idxResponse$rawIdxSt === void 0 ? void 0 : _idxResponse$rawIdxSt.success) || (idxResponse === null || idxResponse === void 0 ? void 0 : (_idxResponse$rawIdxSt2 = idxResponse.rawIdxState) === null || _idxResponse$rawIdxSt2 === void 0 ? void 0 : _idxResponse$rawIdxSt2.successWithInteractionCode);
  }

  restartLoginFlow() {
    // clear all transaction data and saved IDX response
    this.settings.getAuthClient().transactionManager.clear();
    this.appState.set('idx', undefined); // Clear the recoveryToken, if any

    const authClient = this.settings.getAuthClient();
    delete authClient.options['recoveryToken'];
    this.settings.unset('recoveryToken'); // clear otp (email magic link), if any

    this.settings.unset('otp'); // remove all event listeners from current controller instance. A new instance will be created in render().

    this.controller.stopListening(); // Re-render the widget

    this.render(this.controller.constructor);
  }

  start() {
    const pushState = fn$2.supportsPushState();
    Router.prototype.start.call(this, {
      pushState: pushState
    });
  }

  hide() {
    this.header.$el.hide();
  }

  show() {
    this.header.$el.show();
  }

  remove() {
    this.unload();
    this.header.$el.remove();
    this.stopListening(this.appState);
    this.stopListening(this.settings);
    Bundles.remove();
    Backbone.history.stop();
  }

} // Add "Events" to prototype for compatibility with existing code


BaseLoginRouter.prototype.Events = Backbone.Events;

export { BaseLoginRouter as default };
//# sourceMappingURL=BaseLoginRouter.js.map
