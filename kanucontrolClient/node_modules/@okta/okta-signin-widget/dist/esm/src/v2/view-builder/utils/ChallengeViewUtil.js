import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, View, createButton } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Enums from '../../../util/Enums.js';
import Util from '../../../util/Util.js';
import { IDENTIFIER_FLOW, FASTPASS_FALLBACK_SPINNER_TIMEOUT, OV_UV_ENABLE_BIOMETRICS_FASTPASS_MOBILE, OV_UV_ENABLE_BIOMETRICS_FASTPASS_DESKTOP, REQUEST_PARAM_AUTHENTICATION_CANCEL_REASON, LOOPBACK_RESPONSE_STATUS_CODE } from './Constants.js';

function appendLoginHint(deviceChallengeUrl, loginHint) {
  if (deviceChallengeUrl && loginHint) {
    deviceChallengeUrl += '&login_hint=' + loginHint;
  }

  return deviceChallengeUrl;
}
function doChallenge(view, fromView) {
  var _view$options, _view$options$setting;

  const deviceChallenge = view.getDeviceChallengePayload();
  const loginHint = (_view$options = view.options) === null || _view$options === void 0 ? void 0 : (_view$options$setting = _view$options.settings) === null || _view$options$setting === void 0 ? void 0 : _view$options$setting.get('identifier');
  const HIDE_CLASS = 'hide';

  switch (deviceChallenge.challengeMethod) {
    case Enums.LOOPBACK_CHALLENGE:
      view.title = loc('deviceTrust.sso.redirectText', 'login');
      view.add(View.extend({
        className: 'loopback-content',
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            return "<div class=\"spinner\"></div>";
          },
          "useData": true
        })
      }));
      view.doLoopback(deviceChallenge);
      break;

    case Enums.CUSTOM_URI_CHALLENGE:
      view.title = loc('customUri.title', 'login');
      view.add(View.extend({
        className: 'skinny-content',
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return "<p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "customUri.required.content.prompt"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 3
                },
                "end": {
                  "line": 1,
                  "column": 67
                }
              }
            })) + "</p>";
          },
          "useData": true
        })
      }));
      view.add(createButton({
        className: 'ul-button button button-wide button-primary',
        title: loc('oktaVerify.open.button', 'login'),
        id: 'launch-ov',
        click: () => {
          view.doCustomURI();
        }
      }));
      view.add(View.extend({
        className: 'skinny-content',
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var helper,
                alias1 = depth0 != null ? depth0 : container.nullContext || {},
                alias2 = container.hooks.helperMissing,
                alias3 = container.escapeExpression,
                lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return "<p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "customUri.required.content.download.title"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 3
                },
                "end": {
                  "line": 1,
                  "column": 75
                }
              }
            })) + "</p><p><a href=\"" + alias3((helper = (helper = lookupProperty(helpers, "downloadOVLink") || (depth0 != null ? lookupProperty(depth0, "downloadOVLink") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
              "name": "downloadOVLink",
              "hash": {},
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 91
                },
                "end": {
                  "line": 1,
                  "column": 109
                }
              }
            }) : helper)) + "\" target=\"_blank\" id=\"download-ov\" class=\"link\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "customUri.required.content.download.linkText"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 157
                },
                "end": {
                  "line": 1,
                  "column": 232
                }
              }
            })) + "</a></p>";
          },
          "useData": true
        }),
        getTemplateData: function () {
          return {
            downloadOVLink: deviceChallenge.downloadHref
          };
        }
      }));
      view.customURI = appendLoginHint(deviceChallenge.href, loginHint);
      view.doCustomURI();
      break;

    case Enums.UNIVERSAL_LINK_CHALLENGE:
      view.title = loc('universalLink.title', 'login');
      view.add(View.extend({
        className: 'universal-link-content',
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return "<div class=\"spinner\"></div>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "universalLink.content"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 27
                },
                "end": {
                  "line": 1,
                  "column": 79
                }
              }
            }));
          },
          "useData": true
        })
      }));
      view.add(createButton({
        className: 'ul-button button button-wide button-primary',
        title: loc('oktaVerify.open.button', 'login'),
        click: () => {
          // only window.location.href can open universal link in iOS/MacOS
          // other methods won't do, ex, AJAX get or form get (Util.redirectWithFormGet)
          let deviceChallengeUrl = appendLoginHint(deviceChallenge.href, loginHint);
          Util.redirect(deviceChallengeUrl);
        }
      }));
      break;

    case Enums.APP_LINK_CHALLENGE:
      view.title = loc('appLink.title', 'login');
      view.add(View.extend({
        className: 'app-link-content',
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var helper,
                alias1 = depth0 != null ? depth0 : container.nullContext || {},
                alias2 = container.hooks.helperMissing,
                alias3 = "function",
                alias4 = container.escapeExpression,
                lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return "<div class=\"spinner " + alias4((helper = (helper = lookupProperty(helpers, "hideClass") || (depth0 != null ? lookupProperty(depth0, "hideClass") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
              "name": "hideClass",
              "hash": {},
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 20
                },
                "end": {
                  "line": 1,
                  "column": 33
                }
              }
            }) : helper)) + "\"></div><div class=\"appLinkContent " + alias4((helper = (helper = lookupProperty(helpers, "hideClass") || (depth0 != null ? lookupProperty(depth0, "hideClass") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
              "name": "hideClass",
              "hash": {},
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 68
                },
                "end": {
                  "line": 1,
                  "column": 81
                }
              }
            }) : helper)) + "\">" + alias4((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "appLink.content"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 83
                },
                "end": {
                  "line": 1,
                  "column": 129
                }
              }
            })) + "</div>";
          },
          "useData": true
        }),
        getTemplateData: function () {
          return {
            hideClass: HIDE_CLASS
          };
        },
        postRender: function () {
          if (fromView === IDENTIFIER_FLOW) {
            this.$('.spinner').removeClass(HIDE_CLASS);
            setTimeout(oktaUnderscore.bind(() => {
              const data = {
                label: loc('goback', 'login')
              };
              this.options.appState.trigger('updateFooterLink', data);
              this.$('.spinner').addClass(HIDE_CLASS);
              this.$('.appLinkContent').removeClass(HIDE_CLASS);
            }, this), FASTPASS_FALLBACK_SPINNER_TIMEOUT);
          } else {
            this.$('.appLinkContent').removeClass(HIDE_CLASS);
          }
        }
      }));
      view.add(createButton({
        className: `${HIDE_CLASS} al-button button button-wide button-primary`,
        title: loc('oktaVerify.open.button', 'login'),
        click: () => {
          // only window.location.href can open app link in Android
          // other methods won't do, ex, AJAX get or form get (Util.redirectWithFormGet)
          let deviceChallengeUrl = appendLoginHint(deviceChallenge.href, loginHint);
          Util.redirect(deviceChallengeUrl, window, true);
        },
        postRender: function () {
          if (fromView === IDENTIFIER_FLOW) {
            setTimeout(oktaUnderscore.bind(() => {
              this.$el.removeClass(HIDE_CLASS);
            }, this), FASTPASS_FALLBACK_SPINNER_TIMEOUT);
          } else {
            this.$el.removeClass(HIDE_CLASS);
          }
        }
      }));
      break;
  }
}
function cancelPollingWithParams(appState, pollingCancelAction, cancelReason, statusCode) {
  const actionParams = {};
  actionParams[REQUEST_PARAM_AUTHENTICATION_CANCEL_REASON] = cancelReason;
  actionParams[LOOPBACK_RESPONSE_STATUS_CODE] = statusCode;
  appState.trigger('invokeAction', pollingCancelAction, actionParams);
}
function getBiometricsErrorOptions(error, isMessageObj) {
  let errorSummaryKeys;

  if (isMessageObj) {
    var _error$value$;

    errorSummaryKeys = Object.values(error === null || error === void 0 ? void 0 : (_error$value$ = error.value[0]) === null || _error$value$ === void 0 ? void 0 : _error$value$.i18n);
  } else {
    var _error$responseJSON;

    errorSummaryKeys = error === null || error === void 0 ? void 0 : (_error$responseJSON = error.responseJSON) === null || _error$responseJSON === void 0 ? void 0 : _error$responseJSON.errorSummaryKeys;
  }

  const isBiometricsRequiredMobile = errorSummaryKeys && errorSummaryKeys.includes(OV_UV_ENABLE_BIOMETRICS_FASTPASS_MOBILE);
  const isBiometricsRequiredDesktop = errorSummaryKeys && errorSummaryKeys.includes(OV_UV_ENABLE_BIOMETRICS_FASTPASS_DESKTOP);
  let options = [];

  if (!isBiometricsRequiredMobile && !isBiometricsRequiredDesktop) {
    return options;
  }

  const bulletPoints = [loc('oie.authenticator.oktaverify.method.fastpass.verify.enable.biometrics.point1', 'login'), loc('oie.authenticator.oktaverify.method.fastpass.verify.enable.biometrics.point2', 'login'), loc('oie.authenticator.oktaverify.method.fastpass.verify.enable.biometrics.point3', 'login')]; // Add an additional bullet point for desktop devices

  if (isBiometricsRequiredDesktop) {
    bulletPoints.push(loc('oie.authenticator.oktaverify.method.fastpass.verify.enable.biometrics.point4', 'login'));
  }

  options = {
    type: 'error',
    className: 'okta-verify-uv-callout-content',
    title: loc('oie.authenticator.oktaverify.method.fastpass.verify.enable.biometrics.title', 'login'),
    subtitle: loc('oie.authenticator.oktaverify.method.fastpass.verify.enable.biometrics.description', 'login'),
    bullets: bulletPoints
  };
  return options;
}

export { appendLoginHint, cancelPollingWithParams, doChallenge, getBiometricsErrorOptions };
//# sourceMappingURL=ChallengeViewUtil.js.map
