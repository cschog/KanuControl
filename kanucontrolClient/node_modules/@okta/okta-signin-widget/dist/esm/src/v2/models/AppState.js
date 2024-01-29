import '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import Model from '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Logger from '../../util/Logger.js';
import { FORMS_FOR_VERIFICATION, FORMS_WITHOUT_SIGNOUT, FORMS, FORMS_WITH_STATIC_BACK_LINK, AUTHENTICATOR_KEY } from '../ion/RemediationConstants.js';
import { createOVOptions } from '../ion/ui-schema/ion-object-handler.js';
import '../mixins/mixins.js';
import { executeHooksBefore, executeHooksAfter } from '../../util/Hooks.js';

/*!
 * Copyright (c) 2020, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
const UNKNOWN_USER_I8N_KEY = "idx.unknown.user";
/**
 * Keep track of stateMachine with this special model. Similar to `src/models/AppState.js`
 */

const local = {
  user: 'object',
  // optional
  currentFormName: 'string',
  idx: 'object',
  remediations: 'array',
  dynamicRefreshInterval: 'number',
  deviceFingerprint: 'string',
  hooks: 'object' // instance of models/Hooks

};
const derived = {
  authenticatorProfile: {
    deps: ['currentAuthenticator', 'currentAuthenticatorEnrollment'],
    fn: function (currentAuthenticator = {
      profile: undefined
    }, currentAuthenticatorEnrollment = {
      profile: undefined
    }) {
      return currentAuthenticator.profile || currentAuthenticatorEnrollment.profile || {};
    }
  },
  authenticatorKey: {
    deps: ['currentAuthenticator', 'currentAuthenticatorEnrollment'],
    fn: function (currentAuthenticator = {
      key: undefined
    }, currentAuthenticatorEnrollment = {
      key: undefined
    }) {
      return currentAuthenticator.key || currentAuthenticatorEnrollment.key || '';
    }
  },
  authenticatorMethodType: {
    deps: ['currentAuthenticator', 'currentAuthenticatorEnrollment'],
    fn: function (currentAuthenticator = {
      methods: undefined
    }, currentAuthenticatorEnrollment = {
      methods: undefined
    }) {
      return currentAuthenticator.methods && currentAuthenticator.methods[0].type || currentAuthenticatorEnrollment.methods && currentAuthenticatorEnrollment.methods[0].type || '';
    }
  },
  isPasswordRecovery: {
    deps: ['recoveryAuthenticator'],
    fn: function (recoveryAuthenticator = {
      type: undefined
    }) {
      return (recoveryAuthenticator === null || recoveryAuthenticator === void 0 ? void 0 : recoveryAuthenticator.type) === 'password';
    }
  }
};
class AppState extends Model {
  constructor(attributes, options) {
    super(attributes, options);
    this.settings = void 0;
    this.hooks = void 0;
    this.settings = options.settings;
    this.hooks = options.hooks;
  }

  get(attributeName) {
    return Model.prototype.get.call(this, attributeName);
  }

  preinitialize(...args) {
    this.local = local;
    this.derived = derived;
    Model.prototype.preinitialize.apply(this, args);
  }

  isIdentifierOnlyView() {
    var _this$get, _this$get$find, _this$get$find$uiSche;

    return !((_this$get = this.get('remediations')) !== null && _this$get !== void 0 && (_this$get$find = _this$get.find(({
      name: name
    }) => name === 'identify')) !== null && _this$get$find !== void 0 && (_this$get$find$uiSche = _this$get$find.uiSchema) !== null && _this$get$find$uiSche !== void 0 && _this$get$find$uiSche.find(({
      name: name
    }) => name === 'credentials.passcode'));
  }

  hasRemediationObject(formName) {
    return this.get('idx').neededToProceed.find(remediation => remediation.name === formName);
  }

  hasActionObject(actionName) {
    var _this$get2, _this$get2$actions;

    return !!((_this$get2 = this.get('idx')) !== null && _this$get2 !== void 0 && (_this$get2$actions = _this$get2.actions) !== null && _this$get2$actions !== void 0 && _this$get2$actions[actionName]);
  }

  getRemediationAuthenticationOptions(formName) {
    const form = this.hasRemediationObject(formName);

    if (!form) {
      return [];
    }

    const authenticator = form.value.find(value => value.name === 'authenticator');
    let authenticatorOptions = (authenticator === null || authenticator === void 0 ? void 0 : authenticator.options) || []; // OV is a special case, so process OV options

    authenticatorOptions = [...authenticatorOptions]; //clone it since we are changing it for OV

    createOVOptions(authenticatorOptions);
    return authenticatorOptions;
  }

  getActionByPath(actionPath) {
    const paths = actionPath.split('.');
    let targetObject;

    if (paths.length === 1) {
      targetObject = this.get('idx').actions;
    } else {
      targetObject = this.get(paths.shift());
    } // Limitation
    // At the time of writting, action only lives in first level of state objects.


    const actionName = paths.shift();

    if (targetObject && oktaUnderscore.isFunction(targetObject[actionName])) {
      return targetObject[actionName];
    } else {
      return null;
    }
  }

  getCurrentViewState() {
    const currentFormName = this.get('currentFormName');

    if (!currentFormName) {
      return;
    } // didn't expect `remediations` is empty. See `setIonResponse`.


    const currentViewState = this.get('remediations').filter(r => r.name === currentFormName)[0];

    if (!currentViewState) {
      Logger.error('Panic!!');
      Logger.error(`\tCannot find view state for form ${currentFormName}.`);
      const allFormNames = this.get('remediations').map(r => r.name);
      Logger.error(`\tAll available form names: ${allFormNames}`);
    }

    return currentViewState;
  }
  /**
   * Returns ui schema of the form field from current view state
   * @param {string} fieldName
   * @returns {}
   */


  getSchemaByName(fieldName) {
    const currentViewState = this.getCurrentViewState();

    if (currentViewState) {
      const uiSchema = currentViewState.uiSchema;
      return uiSchema.find(({
        name: name
      }) => name === fieldName);
    }
  }
  /**
   * Returns the displayName of the authenticator
   * @returns {string}
   */


  getAuthenticatorDisplayName() {
    const currentAuthenticator = this.get('currentAuthenticator') || {};
    const currentAuthenticatorEnrollment = this.get('currentAuthenticatorEnrollment') || {}; // For enrollment and certain verification flows, the currentAuthenticator object will be present.
    // If not, we're likely in a traditional verify/challenge flow.

    return currentAuthenticator.displayName || currentAuthenticatorEnrollment.displayName;
  }
  /**
   * Checks to see if we're in an authenticator challenge flow.
   * @returns {boolean}
   */


  isAuthenticatorChallenge() {
    const currentFormName = this.get('currentFormName');
    return FORMS_FOR_VERIFICATION.includes(currentFormName);
  }

  shouldReRenderView(transformedResponse) {
    var _transformedResponse$;

    if (transformedResponse !== null && transformedResponse !== void 0 && (_transformedResponse$ = transformedResponse.idx) !== null && _transformedResponse$ !== void 0 && _transformedResponse$.hasFormError) {
      return false;
    }

    const previousRawState = this.has('idx') ? this.get('idx').rawIdxState : null;

    const identicalResponse = oktaUnderscore.isEqual(oktaUnderscore.nestedOmit(transformedResponse.idx.rawIdxState, ['expiresAt', 'refresh', 'stateHandle', 'headers']), oktaUnderscore.nestedOmit(previousRawState, ['expiresAt', 'refresh', 'stateHandle', 'headers']));

    if (identicalResponse) {
      this.set('dynamicRefreshInterval', this.getRefreshInterval(transformedResponse));
    }

    return this._isReRenderRequired(identicalResponse, transformedResponse, previousRawState);
  }

  getRefreshInterval(transformedResponse) {
    var _transformedResponse$2, _transformedResponse$3, _transformedResponse$4, _transformedResponse$5;

    // Only polling refresh interval has changed in the response,
    // make sure to update the correct poll view's refresh value
    const currentFormName = this.get('currentFormName');
    const currentViewState = transformedResponse.remediations.filter(r => r.name === currentFormName)[0]; // Get new refresh interval for either: remediations, authenticator, or authenticator enrollment

    return currentViewState.refresh || ((_transformedResponse$2 = transformedResponse.currentAuthenticatorEnrollment) === null || _transformedResponse$2 === void 0 ? void 0 : (_transformedResponse$3 = _transformedResponse$2.poll) === null || _transformedResponse$3 === void 0 ? void 0 : _transformedResponse$3.refresh) || ((_transformedResponse$4 = transformedResponse.currentAuthenticator) === null || _transformedResponse$4 === void 0 ? void 0 : (_transformedResponse$5 = _transformedResponse$4.poll) === null || _transformedResponse$5 === void 0 ? void 0 : _transformedResponse$5.refresh);
  } // Sign Out link will be displayed in the footer of a form, unless:
  // - widget configuration set hideSignOutLinkInMFA or mfaOnlyFlow to true
  // - cancel remediation form is not present in the response
  // - form is part of our list FORMS_WITHOUT_SIGNOUT


  shouldShowSignOutLinkInCurrentForm(hideSignOutLinkInMFA) {
    const idxActions = this.get('idx') && this.get('idx').actions;
    const currentFormName = this.get('currentFormName');
    return !hideSignOutLinkInMFA && oktaUnderscore.isFunction(idxActions === null || idxActions === void 0 ? void 0 : idxActions.cancel) && !FORMS_WITHOUT_SIGNOUT.includes(currentFormName);
  }

  containsMessageWithI18nKey(keys) {
    if (!Array.isArray(keys)) {
      keys = [keys];
    }

    const messagesObjs = this.get('messages');
    return messagesObjs && Array.isArray(messagesObjs.value) && messagesObjs.value.some(messagesObj => {
      var _messagesObj$i18n;

      return oktaUnderscore.contains(keys, (_messagesObj$i18n = messagesObj.i18n) === null || _messagesObj$i18n === void 0 ? void 0 : _messagesObj$i18n.key);
    });
  }

  containsMessageStartingWithI18nKey(keySubStr) {
    const messagesObjs = this.get('messages');
    return messagesObjs && Array.isArray(messagesObjs.value) && messagesObjs.value.some(messagesObj => {
      var _messagesObj$i18n2;

      return (_messagesObj$i18n2 = messagesObj.i18n) === null || _messagesObj$i18n2 === void 0 ? void 0 : _messagesObj$i18n2.key.startsWith(keySubStr);
    });
  }

  clearAppStateCache() {
    // clear appState before setting new values
    const attrs = {};

    for (const key in this.attributes) {
      if (key !== 'currentFormName') {
        attrs[key] = void 0;
      }
    }

    this.set(attrs, Object.assign({}, {
      unset: true,
      silent: true
    })); // clear cache for derived props.

    this.trigger('cache:clear');
  }

  async setIonResponse(transformedResponse) {
    const doRerender = this.shouldReRenderView(transformedResponse);
    this.clearAppStateCache(); // set new app state properties

    this.set(transformedResponse);

    if (doRerender) {
      var _this$hooks;

      // `currentFormName` is default to first form of remediations or nothing.
      let currentFormName = null;

      if (!oktaUnderscore.isEmpty(transformedResponse.remediations)) {
        currentFormName = transformedResponse.remediations[0].name;
      } else {
        Logger.error('Panic!!');
        Logger.error('\tNo remediation found.');
        Logger.error('\tHere is the entire response');
        Logger.error(JSON.stringify(transformedResponse, null, 2));
      }

      const hook = (_this$hooks = this.hooks) === null || _this$hooks === void 0 ? void 0 : _this$hooks.getHook(currentFormName); // may be undefined

      await executeHooksBefore(hook);
      this.unset('currentFormName', {
        silent: true
      }); // make sure change `currentFormName` is last step.
      // change `currentFormName` will re-render FormController,
      // which may depend on other derived properties hence
      // those derived properties must be re-computed before
      // re-rendering controller.

      this.set({
        currentFormName: currentFormName
      });
      await executeHooksAfter(hook);
    }
  }

  setNonIdxError(error) {
    this.set('remediations', [{
      name: FORMS.TERMINAL
    }]);
    this.set('messages', {
      value: [{
        message: error.errorDetails.errorSummary,
        class: 'ERROR'
      }]
    });
    this.set('currentFormName', FORMS.TERMINAL);
  }

  getUser() {
    return this.get('user');
  }

  _isReRenderRequired(identicalResponse, transformedResponse, previousRawState) {
    var _this$get3;

    let reRender = true;
    const isPreviousStateError = (_this$get3 = this.get('idx')) === null || _this$get3 === void 0 ? void 0 : _this$get3.hasFormError;

    if (isPreviousStateError && this._isChallengeAuthenticatorPoll(transformedResponse, previousRawState)) {
      reRender = false;
    }

    if (identicalResponse) {
      /**
       * returns false: When new response is same as last.
       * usually happens during polling when pipeline doesn't proceed to next step.
       * expiresAt will be different for each response, hence compare objects without that property
       */
      reRender = false;

      if (this.get('currentFormName') === 'poll') {
        /**
         * returns true: We want to force reRender when currentForm is poll because request has to reinitiate
         * based on new refresh and UI has to reflect new timer.
         * We dont technical poll here we just make a request after the specified refresh time each time
         * we get a new response.
         */
        reRender = true;
      } else if (FORMS_WITH_STATIC_BACK_LINK.includes(this.get('currentFormName'))) {
        /**
         * returns true: We want to force reRender if you go back to selection screen from challenge or enroll screen
         * and re-select the same authenticator for challenge. In this case also new response will be identical
         * to the old response.
         */
        reRender = true;
      } else if (this.containsMessageWithI18nKey(UNKNOWN_USER_I8N_KEY)) {
        /**
         * Need to re-render or else form will be stuck in saving mode.
         * This message is a form warning that can result in identical responses if the user enters the same
         * username as the one in the last message warning.
         */
        reRender = true;
      }
    }

    return reRender;
  }
  /**
   * This is to account for the edge case introduced by this issue: OKTA-419210. With the current idx remediations,
   * there's no good way to generalize this as the backend handles the authenticators for phone, sms and 
   * email differently. Although not ideal, we have to keep this check in for now until we find a better solution.
   */


  _isChallengeAuthenticatorPoll(transformedResponse, previousRawState) {
    var _this$get4;

    const isSameExceptMessages = oktaUnderscore.isEqual(oktaUnderscore.nestedOmit(transformedResponse.idx.rawIdxState, ['expiresAt', 'refresh', 'stateHandle', 'headers']), oktaUnderscore.nestedOmit(previousRawState, ['expiresAt', 'refresh', 'stateHandle', 'messages', 'headers']));

    const isChallengeAuthenticator = this.get('currentFormName') === 'challenge-authenticator';
    const isCurrentAuthenticatorEmail = ((_this$get4 = this.get('currentAuthenticatorEnrollment')) === null || _this$get4 === void 0 ? void 0 : _this$get4.key) === AUTHENTICATOR_KEY.EMAIL;
    return isSameExceptMessages && isChallengeAuthenticator && isCurrentAuthenticatorEmail;
  }

}

export { AppState as default };
//# sourceMappingURL=AppState.js.map
