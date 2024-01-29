import _Handlebars2 from '../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, internal } from '../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import Model from '../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import config from '../config/config.json.js';
import Q from 'q';
import fn from '../util/BrowserFeatures.js';
import { ConfigError, UnsupportedBrowserError } from '../util/Errors.js';
import IDP from '../util/IDP.js';
import Logger from '../util/Logger.js';
import Util from '../util/Util.js';
import fn$1 from '../util/CountryUtil.js';

const SharedUtil = internal.util.Util;

const assetBaseUrlTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "https://global.oktacdn.com/okta-signin-widget/" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "version") || (depth0 != null ? lookupProperty(depth0, "version") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "version",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 46
        },
        "end": {
          "line": 1,
          "column": 57
        }
      }
    }) : helper));
  },
  "useData": true
});

const local = {
  baseUrl: ['string', true],
  recoveryToken: ['string', false, undefined],
  stateToken: ['string', false, undefined],
  username: ['string', false],
  relayState: ['string', false],
  // These two settings are aliases. Setting either value will set `backToSignInUri` 
  signOutLink: ['string', false],
  // for backward compatibility
  backToSignInLink: ['string', false],
  // preferred setting
  redirect: {
    type: 'string',
    values: ['never', 'always', 'auto'],
    value: 'auto'
  },
  // allows bootstrapping the Widget into a specific view such
  // as register or forgot password
  flow: ['string', false, 'default'],
  // Function to transform the username before passing it to the API
  // for Primary Auth, Forgot Password and Unlock Account.
  transformUsername: ['function', false],
  // CALLBACKS
  globalSuccessFn: 'function',
  globalErrorFn: 'function',
  processCreds: 'function',
  hooks: 'object',
  // IMAGES
  logo: 'string',
  logoText: ['string', false],
  helpSupportNumber: 'string',
  // <OIE>
  // attribute to hold proxy (fake) idx response
  // to render static pages without initiating idx pipeline
  proxyIdxResponse: ['object', false],
  // <OIE>
  // By default, state handle will be saved to session storage
  // and will be clear when terminal error or success redirect.
  // Set this flag to true if you want to override this behavior.
  // a.k.a dishonor the state handle stored in session storage.
  overrideExistingStateToken: ['boolean', false, false],
  // FEATURES
  'features.router': ['boolean', true, false],
  'features.securityImage': ['boolean', true, false],
  'features.rememberMe': ['boolean', true, true],
  'features.autoPush': ['boolean', true, false],
  'features.smsRecovery': ['boolean', true, false],
  'features.callRecovery': ['boolean', true, false],
  'features.emailRecovery': ['boolean', false, true],
  'features.webauthn': ['boolean', true, false],
  'features.selfServiceUnlock': ['boolean', true, false],
  'features.multiOptionalFactorEnroll': ['boolean', true, false],
  'features.deviceFingerprinting': ['boolean', false, false],
  'features.hideSignOutLinkInMFA': ['boolean', false, false],
  'features.skipIdpFactorVerificationBtn': ['boolean', false, false],
  'features.hideBackToSignInForReset': ['boolean', false, false],
  'features.customExpiredPassword': ['boolean', true, false],
  'features.registration': ['boolean', false, false],
  'features.idpDiscovery': ['boolean', false, false],
  'features.passwordlessAuth': ['boolean', false, false],
  'features.showPasswordToggleOnSignInPage': ['boolean', false, false],
  'features.trackTypingPattern': ['boolean', false, false],
  'features.redirectByFormSubmit': ['boolean', false, false],
  'features.useDeviceFingerprintForSecurityImage': ['boolean', false, true],
  'features.showPasswordRequirementsAsHtmlList': ['boolean', false, false],
  'features.mfaOnlyFlow': ['boolean', false, false],
  'features.scrollOnError': ['boolean', false, true],
  'features.showKeepMeSignedIn': ['boolean', false, true],
  'features.showIdentifier': ['boolean', false, true],
  'features.autoFocus': ['boolean', false, true],
  'features.rememberMyUsernameOnOIE': ['boolean', false, false],
  defaultCountryCode: ['string', false, 'US'],
  // I18N
  language: ['any', false],
  // Can be a string or a function
  i18n: ['object', false],
  // ASSETS
  'assets.baseUrl': ['string', false],
  'assets.rewrite': {
    type: 'function',
    value: oktaUnderscore.identity
  },
  'assets.languages': ['array', false],
  // OAUTH2
  issuer: 'string',
  clientId: 'string',
  redirectUri: 'string',
  state: 'string',
  nonce: 'string',
  scopes: 'array',
  codeChallenge: 'string',
  codeChallengeMethod: ['string', false],
  oAuthTimeout: ['number', false],
  authScheme: ['string', false, 'OAUTH2'],
  // External IdPs
  idps: ['array', false, []],
  idpDisplay: {
    type: 'string',
    values: ['PRIMARY', 'SECONDARY'],
    value: 'SECONDARY'
  },
  // HELP LINKS
  'helpLinks.help': 'string',
  'helpLinks.forgotPassword': 'string',
  'helpLinks.unlock': 'string',
  'helpLinks.custom': 'array',
  'helpLinks.factorPage.href': 'string',
  'helpLinks.factorPage.text': 'string',
  //Custom Buttons
  customButtons: ['array', false, []],
  //Registration
  policyId: 'string',
  'registration.click': 'function',
  'registration.parseSchema': 'function',
  'registration.preSubmit': 'function',
  'registration.postSubmit': 'function',
  //Consent
  'consent.cancel': 'function',
  //IDP Discovery
  'idpDiscovery.requestContext': 'string',
  //Colors
  'colors.brand': 'string',
  //Descriptions
  brandName: 'string',
  //PIV
  piv: ['object', false, {}],
  //Email verify callback
  otp: 'string'
};
const derived = {
  backToSignInUri: {
    deps: ['backToSignInLink', 'signOutLink'],
    fn: function (backToSignInLink, signOutLink) {
      return backToSignInLink || signOutLink; // prefer backToSignInLink over signOutLink, but they are aliases
    }
  },
  showPasswordToggle: {
    deps: ['features.showPasswordToggleOnSignInPage'],
    fn: function () {
      var _this$options$feature, _this$options, _this$options$feature2, _this$options2;

      // showPasswordToggle is for OIE only.
      // Used to default showPasswordToggleOnSignInPage to true.
      const defaultValue = true;
      const customizedValue = (_this$options$feature = (_this$options = this.options) === null || _this$options === void 0 ? void 0 : (_this$options$feature2 = _this$options.features) === null || _this$options$feature2 === void 0 ? void 0 : _this$options$feature2.showPasswordToggleOnSignInPage) !== null && _this$options$feature !== void 0 ? _this$options$feature : (_this$options2 = this.options) === null || _this$options2 === void 0 ? void 0 : _this$options2['features.showPasswordToggleOnSignInPage'];
      return customizedValue !== null && customizedValue !== void 0 ? customizedValue : defaultValue;
    },
    cache: true
  },
  redirectUtilFn: {
    deps: ['features.redirectByFormSubmit'],
    fn: function (redirectByFormSubmit) {
      return redirectByFormSubmit ? Util.redirectWithFormGet.bind(Util) : SharedUtil.redirect.bind(SharedUtil);
    },
    cache: true
  },
  supportedLanguages: {
    deps: ['i18n', 'language', 'assets.languages'],
    fn: function (i18n, language, hostedLanguages) {
      // By default, the language be automatically detected from the browser
      // Developers can specify the language. It will be added to the supportedLanguages list.
      // Developers can also provide a list of languages with hosted assets, these replace the default list
      const supportedLanguages = hostedLanguages || config.supportedLanguages;
      return oktaUnderscore.union(supportedLanguages, oktaUnderscore.keys(i18n), oktaUnderscore.isString(language) ? [language] : []);
    },
    cache: true
  },
  languageCode: {
    deps: ['language', 'supportedLanguages'],
    fn: function (language, supportedLanguages) {
      const userLanguages = fn.getUserLanguages(); // TODO: revisit this fix - OKTA-491150

      userLanguages.forEach((val, idx) => {
        if (val === 'nl') {
          userLanguages[idx] = 'nl-NL';
        } else if (val === 'pt') {
          userLanguages[idx] = 'pt-BR';
        }
      });

      const preferred = oktaUnderscore.clone(userLanguages);

      const supportedLowerCase = Util.toLower(supportedLanguages); // Any developer defined "language" takes highest priority:
      // As a string, i.e. 'en', 'ja', 'zh-CN'

      if (oktaUnderscore.isString(language)) {
        preferred.unshift(language);
      } else if (oktaUnderscore.isFunction(language)) {
        // As a callback function, which is passed the list of supported
        // languages and detected user languages. This function must return
        // a languageCode, i.e. 'en', 'ja', 'zh-CN'
        preferred.unshift(language(supportedLanguages, userLanguages));
      } // Add default language, and expand to include any language
      // codes that do not include region, dialect, etc.


      preferred.push(config.defaultLanguage);
      const expanded = Util.toLower(Util.expandLanguages(preferred)); // Perform a case insensitive search - this is necessary in the case
      // of browsers like Safari

      let i;
      let supportedPos;

      for (i = 0; i < expanded.length; i++) {
        supportedPos = supportedLowerCase.indexOf(expanded[i]);

        if (supportedPos > -1) {
          return supportedLanguages[supportedPos];
        }
      }
    }
  },
  countryCode: {
    deps: ['defaultCountryCode'],
    fn: function (defaultCountryCode) {
      const countries = fn$1.getCountries();
      return Object.keys(countries).includes(defaultCountryCode) ? defaultCountryCode : 'US';
    }
  },
  mode: {
    deps: ['useInteractionCodeFlow', 'codeChallenge'],
    fn: function (useInteractionCodeFlow, codeChallenge) {
      if (useInteractionCodeFlow && codeChallenge) {
        return 'remediation';
      }

      return 'relying-party';
    }
  },
  oauth2Enabled: {
    deps: ['clientId', 'authScheme'],
    fn: function (clientId, authScheme) {
      return !!clientId && authScheme.toLowerCase() === 'oauth2';
    },
    cache: true
  },
  oieEnabled: {
    deps: ['stateToken', 'proxyIdxResponse', 'useInteractionCodeFlow'],
    fn: function (stateToken, proxyIdxResponse, useInteractionCodeFlow) {
      return stateToken || proxyIdxResponse || useInteractionCodeFlow;
    },
    cache: true
  },
  // Redirect Uri to provide in the oauth API
  oauthRedirectUri: {
    deps: ['redirectUri'],
    fn: function (redirectUri) {
      if (redirectUri) {
        return redirectUri;
      }

      let origin = window.location.origin; // IE8

      if (!origin) {
        const href = window.location.href;
        const path = window.location.pathname;

        if (path !== '') {
          origin = href.substring(0, href.lastIndexOf(path));
        }
      }

      return encodeURI(origin);
    }
  },
  // Adjusts the idps passed into the widget based on if they get explicit support
  configuredSocialIdps: {
    deps: ['idps'],
    fn: function (idps) {
      return oktaUnderscore.map(idps, function (idpConfig) {
        const idp = oktaUnderscore.clone(idpConfig);

        let type = idp.type && idp.type.toLowerCase();

        if (!(type && oktaUnderscore.contains(IDP.SUPPORTED_SOCIAL_IDPS, type))) {
          type = 'general-idp';
          idp.text = idp.text || '{ Please provide a text value }';
        }

        idp.className = ['social-auth-button', 'social-auth-' + type + '-button ', idp.className ? idp.className : ''].join(' ');
        idp.dataAttr = 'social-auth-' + type + '-button';
        idp.i18nKey = 'socialauth.' + type + '.label';
        return idp;
      });
    },
    cache: true
  },
  // Can support piv authentication
  hasPivCard: {
    deps: ['piv'],
    fn: function (piv) {
      return piv && piv.certAuthUrl;
    },
    cache: true
  },
  // social auth buttons order - 'above'/'below' the primary auth form (boolean)
  socialAuthPositionTop: {
    deps: ['configuredSocialIdps', 'hasPivCard', 'idpDisplay'],
    fn: function (configuredSocialIdps, hasPivCard, idpDisplay) {
      return (!oktaUnderscore.isEmpty(configuredSocialIdps) || hasPivCard) && idpDisplay.toUpperCase() === 'PRIMARY';
    },
    cache: true
  },
  hasConfiguredButtons: {
    deps: ['configuredSocialIdps', 'customButtons', 'hasPivCard'],
    fn: function (configuredSocialIdps, customButtons, hasPivCard) {
      return !oktaUnderscore.isEmpty(configuredSocialIdps) || !oktaUnderscore.isEmpty(customButtons) || hasPivCard;
    },
    cache: true
  }
};
class Settings extends Model {
  constructor(...args) {
    super(...args);
    this.authClient = void 0;
  }

  get(attributeName) {
    return Model.prototype.get.call(this, attributeName);
  }

  preinitialize(...args) {
    this.local = local;
    this.derived = derived;
    Model.prototype.preinitialize.apply(this, args);
  }

  initialize(options) {
    const {
      colors: colors
    } = options;
    let {
      baseUrl: baseUrl
    } = options;

    if (!baseUrl) {
      // infer baseUrl from the issuer
      const {
        authClient: authClient
      } = options;

      if (authClient) {
        baseUrl = authClient.getIssuerOrigin();
      } else {
        var _issuer;

        // issuer can be passed at top-level or in authParams
        const {
          authParams: authParams
        } = options;
        let {
          issuer: issuer
        } = options;
        issuer = issuer || (authParams === null || authParams === void 0 ? void 0 : authParams.issuer);
        baseUrl = (_issuer = issuer) === null || _issuer === void 0 ? void 0 : _issuer.split('/oauth2/')[0];
      }

      this.set('baseUrl', baseUrl);
    }

    if (!baseUrl) {
      this.callGlobalError(new ConfigError(loc('error.required.baseUrl')));
    } else if (colors && oktaUnderscore.isString(colors.brand) && !colors.brand.match(/^#[0-9A-Fa-f]{6}$/)) {
      this.callGlobalError(new ConfigError(loc('error.invalid.colors.brand')));
    } else if (fn.corsIsNotSupported()) {
      this.callGlobalError(new UnsupportedBrowserError(loc('error.unsupported.cors')));
    }
  }

  setAcceptLanguageHeader(authClient) {
    if (authClient) {
      authClient.http.setRequestHeader('Accept-Language', this.get('languageCode'));
    }
  }

  setAuthClient(authClient) {
    this.setAcceptLanguageHeader(authClient);
    this.authClient = authClient;
  }

  getAuthClient() {
    return this.authClient;
  }

  set(...args) {
    try {
      return Model.prototype.set.apply(this, args);
    } catch (e) {
      const message = e.message ? e.message : e;
      this.callGlobalError(new ConfigError(message));
    }
  } // Invokes the global success function. This should only be called on a
  // terminal part of the code (i.e. authStatus SUCCESS or after sending
  // a recovery email)


  callGlobalSuccess(status, data) {
    const res = oktaUnderscore.extend(data, {
      status: status
    }); // Defer this to ensure that our functions have rendered completely
    // before invoking their function


    oktaUnderscore.defer(oktaUnderscore.partial(this.get('globalSuccessFn'), res));
  } // Invokes the global error function. This should only be called on non
  // recoverable errors (i.e. configuration errors, browser unsupported
  // errors, etc)


  callGlobalError(err) {
    const globalErrorFn = this.get('globalErrorFn') || this.options.globalErrorFn; // Note: Must use "this.options.globalErrorFn" when they've passed invalid
    // arguments - globalErrorFn will not have been set yet

    if (globalErrorFn) {
      globalErrorFn(err);
    } else {
      // Only throw the error if they have not registered a globalErrorFn
      throw err;
    }
  } // Get the username by applying the transform function if it exists.


  transformUsername(username, operation) {
    const transformFn = this.get('transformUsername');

    if (transformFn && oktaUnderscore.isFunction(transformFn)) {
      return transformFn(username, operation);
    }

    return username;
  }

  processCreds(creds) {
    const processCreds = this.get('processCreds');
    return Q.Promise(function (resolve) {
      if (!oktaUnderscore.isFunction(processCreds)) {
        resolve();
      } else if (processCreds.length === 2) {
        processCreds(creds, resolve);
      } else {
        processCreds(creds);
        resolve();
      }
    });
  }

  parseRegistrationSchema(schema, onSuccess, onFailure) {
    const parseSchema = this.get('registration.parseSchema'); //check for parseSchema callback

    if (oktaUnderscore.isFunction(parseSchema)) {
      parseSchema(schema, function (schema) {
        onSuccess(schema);
      }, function (error) {
        error = error || {
          errorSummary: loc('registration.default.callbackhook.error')
        };
        error['callback'] = 'parseSchema';
        onFailure(error);
      });
    } else {
      //no callback
      onSuccess(schema);
    }
  }

  preRegistrationSubmit(postData, onSuccess, onFailure) {
    const preSubmit = this.get('registration.preSubmit'); //check for preSubmit callback

    if (oktaUnderscore.isFunction(preSubmit)) {
      preSubmit(postData, function (postData) {
        onSuccess(postData);
      }, function (error) {
        error = error || {
          errorSummary: loc('registration.default.callbackhook.error')
        };
        error['callback'] = 'preSubmit';
        onFailure(error);
      });
    } else {
      //no callback
      onSuccess(postData);
    }
  }

  postRegistrationSubmit(response, onSuccess, onFailure) {
    const postSubmit = this.get('registration.postSubmit'); //check for postSubmit callback

    if (oktaUnderscore.isFunction(postSubmit)) {
      postSubmit(response, function (response) {
        onSuccess(response);
      }, function (error) {
        error = error || {
          errorSummary: loc('registration.default.callbackhook.error')
        };
        error['callback'] = 'postSubmit';
        onFailure(error);
      });
    } else {
      //no callback
      onSuccess(response);
    }
  } // Use the parse function to transform config options to the standard
  // settings we currently support. This is a good place to deprecate old
  // option formats.


  parse(options) {
    if (options.labels || options.country) {
      Logger.deprecate('Use "i18n" instead of "labels" and "country"');
      const overrides = options.labels || {};

      oktaUnderscore.each(options.country, function (val, key) {
        overrides['country.' + key] = val;
      }); // Old behavior is to treat the override as a global override, so we
      // need to add these overrides to each language


      options.i18n = {};

      oktaUnderscore.each(config.supportedLanguages, function (language) {
        options.i18n[language] = overrides;
      });

      delete options.labels;
      delete options.country;
    } // Default the assets.baseUrl to the cdn, or remove any trailing slashes


    if (!options.assets) {
      options.assets = {};
    }

    const abu = options.assets.baseUrl;

    if (!abu) {
      options.assets.baseUrl = assetBaseUrlTpl({
        version: config.version
      });
    } else if (abu[abu.length - 1] === '/') {
      options.assets.baseUrl = abu.substring(0, abu.length - 1);
    }

    return options;
  }

  isDsTheme() {
    return false;
  }

}

export { Settings as default };
//# sourceMappingURL=Settings.js.map
