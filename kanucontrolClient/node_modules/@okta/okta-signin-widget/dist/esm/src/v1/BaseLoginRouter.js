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
import AppState from './models/AppState.js';
import Settings from '../models/Settings.js';
import Bundles from '../util/Bundles.js';
import Logger from '../util/Logger.js';
import AuthContainer from './views/shared/AuthContainer.js';
import Header from './views/shared/Header.js';
import SecurityBeacon from './views/shared/SecurityBeacon.js';
import fn$2 from '../util/Animations.js';
import fn$3 from '../util/BrowserFeatures.js';
import fn$1 from '../util/ColorsUtil.js';
import Enums from '../util/Enums.js';
import { ConfigError } from '../util/Errors.js';
import fn from './util/RouterUtil.js';
import Util from '../util/Util.js';
import LanguageUtil from '../util/LanguageUtil.js';

/*!
 * Copyright (c) 2015-2016, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

function isStateLessRouteHandler(router, fn) {
  return oktaUnderscore.find(router.stateLessRouteHandlers, function (routeName) {
    return fn === router[routeName];
  });
}

function beaconIsAvailable(Beacon, settings) {
  if (!Beacon) {
    return false;
  }

  if (Beacon === SecurityBeacon) {
    return settings.get('features.securityImage');
  }

  return true;
}

var BaseLoginRouter = Router.extend({
  Events: Backbone.Events,
  initialize: function (options) {
    // Create a default success and/or error handler if
    // one is not provided.
    if (!options.globalSuccessFn) {
      options.globalSuccessFn = function () {};
    }

    if (!options.globalErrorFn) {
      options.globalErrorFn = function (err) {
        Logger.error(err);
      };
    }

    this.settings = new Settings(oktaUnderscore.omit(options, 'el', 'authClient'), {
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
    this.appState = new AppState({
      baseUrl: this.settings.get('baseUrl'),
      settings: this.settings
    }, {
      parse: true
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
    this.listenTo(this.appState, 'change:transactionError', function (appState, err) {
      fn.routeAfterAuthStatusChangeError(this, err);
    });
    this.listenTo(this.appState, 'change:transaction', function (appState, trans) {
      fn.routeAfterAuthStatusChange(this, trans.data);
    });
    this.listenTo(this.appState, 'navigate', function (url) {
      this.navigate(url, {
        trigger: true
      });
    });
  },
  execute: function (cb, args) {
    const recoveryToken = this.settings.get('recoveryToken'); // Recovery flow with a token passed through widget settings

    if (recoveryToken) {
      this.settings.unset('recoveryToken');
      this.navigate(fn.createRecoveryUrl(recoveryToken), {
        trigger: true
      });
      return;
    } // Refresh flow with a stateToken passed through widget settings


    const stateToken = this.settings.get('stateToken');

    if (stateToken) {
      this.settings.unset('stateToken');
      this.navigate(fn.createRefreshUrl(stateToken), {
        trigger: true
      });
      return;
    } // Normal flow - we've either navigated to a stateless page, or are
    // in the middle of an auth flow


    const trans = this.appState.get('transaction');

    if (trans && trans.data || isStateLessRouteHandler(this, cb)) {
      cb.apply(this, args);
      return;
    } // StateToken cookie exists on page load, and we are on a stateful url


    if (this.settings.getAuthClient().tx.exists()) {
      this.navigate(fn.createRefreshUrl(), {
        trigger: true
      });
      return;
    } // We've hit a page that requires state, but have no stateToken - redirect
    // back to primary auth


    this.navigate('', {
      trigger: true
    });
  },
  // Overriding the default navigate method to allow the widget consumer
  // to "turn off" routing - if features.router is false, the browser
  // location bar will not update when the router navigates
  navigate: function (fragment, options) {
    if (this.settings.get('features.router')) {
      return Router.prototype.navigate.apply(this, arguments);
    }

    if (options && options.trigger) {
      return Backbone.history.loadUrl(fragment);
    }
  },
  render: function (Controller, options) {
    options || (options = {});
    let Beacon = options.Beacon;

    const controllerOptions = oktaUnderscore.extend({
      settings: this.settings,
      appState: this.appState
    }, oktaUnderscore.omit(options, 'Beacon')); // Since we have a wrapper view, render our wrapper and use its content
    // element as our new el.
    // Note: Render it here because we know dom is ready at this point


    if (!this.header.rendered()) {
      this.el = this.header.render().getContentEl();
    } // If we need to load a language (or apply custom i18n overrides), do
    // this now and re-run render after it's finished.


    if (!Bundles.isLoaded(this.appState.get('languageCode'))) {
      return LanguageUtil.loadLanguage(this.appState, this.settings).then(oktaUnderscore.bind(this.render, this, Controller, options));
    } // Load the custom colors only on the first render


    if (this.settings.get('colors.brand') && !fn$1.isLoaded()) {
      const colors = {
        brand: this.settings.get('colors.brand')
      };
      fn$1.addStyle(colors);
    }

    const oldController = this.controller;
    this.controller = new Controller(controllerOptions); // Bubble up all controller events

    this.listenTo(this.controller, 'all', this.trigger); // First run fetchInitialData, in case the next controller needs data
    // before it's initial render. This will leave the current page in a
    // loading state.

    return this.controller.fetchInitialData().then(() => {
      // Beacon transition occurs in parallel to page swap
      if (!beaconIsAvailable(Beacon, this.settings)) {
        Beacon = null;
      }

      this.header.setBeacon(Beacon, controllerOptions); // Show before initial render

      this.show();
      this.controller.render();

      if (!oldController) {
        this.el.append(this.controller.el);
        this.controller.postRenderAnimation();
        return;
      }

      return fn$2.swapPages({
        $parent: this.el,
        $oldRoot: oldController.$el,
        $newRoot: this.controller.$el,
        dir: oldController.state.get('navigateDir'),
        ctx: this,
        success: function () {
          const flashError = this.appState.get('flashError');
          const model = this.controller.model;
          oldController.remove();
          oldController.$el.remove();
          this.controller.postRenderAnimation();

          if (flashError) {
            let errorSummary;
            const isTerminalError = flashError.is && flashError.is('terminal');

            if (isTerminalError) {
              errorSummary = flashError.errorDetails.errorSummary;
            } else {
              errorSummary = loc(this.settings.get('features.mfaOnlyFlow') ? 'error.mfa.only.expired.session' : 'error.expired.session');
            }

            model.trigger('error', model, {
              responseJSON: {
                errorSummary: errorSummary
              }
            });
            this.appState.unset('flashError');
            Util.triggerAfterError(this.controller, flashError);
          }
        }
      });
    }).catch(function () {
      // OKTA-69665 - if an error occurs in fetchInitialData, we're left in
      // a state with two active controllers. Therefore, we clean up the
      // old one. Note: This explicitly handles the invalid token case -
      // if we get some other type of error which doesn't force a redirect,
      // we will probably be left in a bad state. I.e. old controller is
      // dropped and new controller is not rendered.
      if (oldController) {
        oldController.remove();
        oldController.$el.remove();
      }
    });
  },
  start: function () {
    let pushState = false; // Support for browser's back button.

    if (window.addEventListener && this.settings.get('features.router')) {
      window.addEventListener('popstate', e => {
        if (this.controller.back) {
          e.preventDefault();
          e.stopImmediatePropagation();
          this.controller.back();
        }
      });
      pushState = fn$3.supportsPushState();
    }

    Router.prototype.start.call(this, {
      pushState: pushState
    });
  },
  hide: function () {
    this.header.$el.hide();
  },
  show: function () {
    this.header.$el.show();
  },
  remove: function () {
    if (this.controller) {
      this.controller.remove();
    }

    this.header.$el.remove();
    Bundles.remove();
    Backbone.history.stop();
  }
});

export { BaseLoginRouter as default };
//# sourceMappingURL=BaseLoginRouter.js.map
