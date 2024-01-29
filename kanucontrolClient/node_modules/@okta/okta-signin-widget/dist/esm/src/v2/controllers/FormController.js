import { Controller, loc } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import ViewFactory from '../view-builder/ViewFactory.js';
import IonResponseHelper from '../ion/IonResponseHelper.js';
import { getV1ClassName } from '../ion/ViewClassNamesFactory.js';
import { TERMINAL_FORMS, FORMS, FORM_NAME_TO_OPERATION_MAP } from '../ion/RemediationConstants.js';
import Util from '../../util/Util.js';
import sessionStorageHelper from '../client/sessionStorageHelper.js';
import { IdxStatus } from '@okta/okta-auth-js';
import { CONFIGURED_FLOW } from '../client/constants.js';
import { ConfigError } from '../../util/Errors.js';
import { updateAppState } from '../client/updateAppState.js';

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
var FormController = Controller.extend({
  className: 'form-controller',
  appStateEvents: {
    'change:currentFormName': 'handleFormNameChange',
    'afterError': 'handleAfterError',
    'invokeAction': 'handleInvokeAction',
    'saveForm': 'handleSaveForm',
    'switchForm': 'handleSwitchForm'
  },
  preRender: function () {
    this.removeChildren();
  },
  postRender: function () {
    const currentViewState = this.options.appState.getCurrentViewState(); // TODO: add comments regarding when `currentViewState` would be null?

    if (!currentViewState) {
      return;
    }

    this.clearMetadata();
    let formName = currentViewState.name;

    if (formName === 'identify' && this.options.settings.get('flow') === CONFIGURED_FLOW.RESET_PASSWORD) {
      formName = 'identify-recovery';
    }

    const TheView = ViewFactory.create(formName, this.options.appState.get('authenticatorKey'));

    try {
      this.formView = this.add(TheView, {
        options: {
          currentViewState: currentViewState
        }
      }).last();
    } catch (error) {
      // This is the place where runtime error (NPE) happens at most of time.
      // It has been swallowed by Q.js hence add try/catch to surface up errors.
      this.options.settings.callGlobalError(error);
      return;
    }

    this.triggerAfterRenderEvent();
  },
  clearMetadata: function () {
    const formName = this.options.appState.get('currentFormName'); // TODO: OKTA-392835 shall not clear state handle at terminal page

    if (TERMINAL_FORMS.includes(formName)) {
      sessionStorageHelper.removeStateHandle();
    }
  },
  triggerAfterRenderEvent: function () {
    const contextData = this.createAfterEventContext();
    this.trigger('afterRender', contextData);
  },
  handleFormNameChange: function () {
    this.render();
  },
  handleAfterError: function (error) {
    const contextData = this.createAfterEventContext();
    const errorContextData = {
      xhr: error,
      errorSummary: error.responseJSON && error.responseJSON.errorSummary
    }; // TODO: need some enhancement after https://github.com/okta/okta-idx-js/pull/27
    // OKTA-318062

    this.trigger('afterError', contextData, errorContextData);
  },
  createAfterEventContext: function () {
    const formName = this.options.appState.get('currentFormName');
    const authenticatorKey = this.options.appState.get('authenticatorKey');
    const methodType = this.options.appState.get('authenticatorMethodType');
    const isPasswordRecoveryFlow = this.options.appState.get('isPasswordRecoveryFlow');
    const v1ControllerClassName = getV1ClassName(formName, authenticatorKey, methodType, isPasswordRecoveryFlow);
    const eventData = {
      controller: v1ControllerClassName,
      formName: formName
    };

    if (authenticatorKey) {
      eventData.authenticatorKey = authenticatorKey;
    }

    if (methodType) {
      eventData.methodType = methodType;
    }

    return eventData;
  },
  handleSwitchForm: function (formName) {
    // trigger formName change to change view
    if (this.options.appState.get('messages')) {
      // Clear messages before calling switch form.
      // If a form has errors sent form API inside messages
      // and user hits back to factors list which triggers switchForm,
      // those error will show up on another screen that gets rendered after switchForm
      this.options.appState.unset('messages');
    }

    this.options.appState.set('currentFormName', formName);
  },
  // eslint-disable-next-line max-statements
  handleInvokeAction: async function (actionPath = '', actionParams = {}) {
    const {
      appState: appState,
      settings: settings
    } = this.options;
    const idx = appState.get('idx');
    const {
      stateHandle: stateHandle
    } = idx.context;
    let invokeOptions = {
      exchangeCodeForTokens: false,
      // we handle this in interactionCodeFlow.js
      shouldProceedWithEmailAuthenticator: false,
      // do not auto-select email authenticator
      stateHandle: stateHandle
    };
    let error; // Cancel action is executes synchronously

    if (actionPath === 'cancel') {
      // TODO: resolve race conditions caused by event pattern: OKTA-490220
      settings.getAuthClient().transactionManager.clear({
        clearIdxResponse: false
      });
      sessionStorageHelper.removeStateHandle();
      appState.clearAppStateCache();

      if (settings.get('useInteractionCodeFlow')) {
        // In this case we need to restart login flow and recreate transaction meta
        // that will be used in interactionCodeFlow function
        appState.trigger('restartLoginFlow');
        return;
      }
    } // Build options to invoke or throw error for invalid action


    if (idx['neededToProceed'].find(item => item.name === actionPath)) {
      invokeOptions = { ...invokeOptions,
        step: actionPath
      };
    } else if (oktaUnderscore.isFunction(idx['actions'][actionPath])) {
      invokeOptions = { ...invokeOptions,
        actions: [{
          name: actionPath,
          params: actionParams
        }]
      };
    } else {
      error = new ConfigError(`Invalid action selected: ${actionPath}`);
      this.options.settings.callGlobalError(error);
      await this.showFormErrors(this.formView.model, error, this.formView.form);
      return;
    } // action will be executed asynchronously


    await this.invokeAction(invokeOptions);
  },
  invokeAction: async function (invokeOptions) {
    const authClient = this.options.settings.getAuthClient();
    let resp;
    let error;

    try {
      resp = await authClient.idx.proceed(invokeOptions);

      if (resp.requestDidSucceed === false) {
        error = resp;
      }
    } catch (e) {
      error = e;
    } // if request did not succeed, show error on the current form


    if (error) {
      await this.showFormErrors(this.formView.model, error, this.formView.form);
      return;
    } // process response, may render a new form


    await this.handleIdxResponse(resp);
  },
  // eslint-disable-next-line max-statements, complexity
  handleSaveForm: async function (model) {
    const formName = model.get('formName'); // Toggle Form saving status (e.g. disabling save button, etc)

    this.toggleFormButtonState(true);
    model.trigger('request'); // Use full page redirection if necessary

    if (model.get('useRedirect')) {
      // Clear when navigates away from SIW page, e.g. success, IdP Authenticator.
      // Because SIW sort of finished its current /transaction/
      sessionStorageHelper.removeStateHandle();
      const currentViewState = this.options.appState.getCurrentViewState();
      Util.redirectWithFormGet(currentViewState.href);
      return;
    } // Run hook: transform the user name (a.k.a identifier)


    const values = this.transformIdentifier(formName, model); // Error out when this is not a remediation form. Unexpected Exception.

    if (!this.options.appState.hasRemediationObject(formName)) {
      this.options.settings.callGlobalError(`Cannot find http action for "${formName}".`);
      await this.showFormErrors(this.formView.model, 'Cannot find action to proceed.', this.formView.form);
      return;
    } // Reset password in identity-first flow needs some help to auto-select password and begin the reset flow


    if (formName === 'identify' && this.options.settings.get('flow') === CONFIGURED_FLOW.RESET_PASSWORD) {
      values.authenticator = 'okta_password';
    } // Submit request to idx endpoint


    const authClient = this.options.settings.getAuthClient();
    const idxOptions = {
      exchangeCodeForTokens: false,
      // we handle this in interactionCodeFlow.js
      shouldProceedWithEmailAuthenticator: false // do not auto-select email authenticator

    };

    try {
      const idx = this.options.appState.get('idx');
      const {
        stateHandle: stateHandle
      } = idx.context;
      const resp = await authClient.idx.proceed({ ...idxOptions,
        step: formName,
        stateHandle: stateHandle,
        ...values
      });

      if (resp.status === IdxStatus.FAILURE) {
        throw resp.error; // caught and handled in this function
      } // follow idx transaction to render terminal view for session expired error


      if (IonResponseHelper.isIdxSessionExpiredError(resp)) {
        const authClient = this.settings.getAuthClient();
        authClient.transactionManager.clear();
        await this.handleIdxResponse(resp);
        return;
      } // If the last request did not succeed, show errors on the current form
      // Special case: Okta server responds 401 status code with WWW-Authenticate header and new remediation
      // so that the iOS/MacOS credential SSO extension (Okta Verify) can intercept
      // the response reaches here when Okta Verify is not installed
      // we need to return an idx object so that
      // the SIW can proceed to the next step without showing error


      if (resp.requestDidSucceed === false && !resp.stepUp) {
        await this.showFormErrors(model, resp, this.formView.form);
        return;
      }

      const onSuccess = this.handleIdxResponse.bind(this, resp);

      if (formName === FORMS.ENROLL_PROFILE) {
        var _values$userProfile;

        // call registration (aka enroll profile) hook
        this.settings.postRegistrationSubmit(values === null || values === void 0 ? void 0 : (_values$userProfile = values.userProfile) === null || _values$userProfile === void 0 ? void 0 : _values$userProfile.email, onSuccess, error => {
          model.trigger('error', model, {
            responseJSON: error
          });
        });
      } else {
        await onSuccess();
      }
    } catch (error) {
      var _error$is;

      if ((_error$is = error.is) !== null && _error$is !== void 0 && _error$is.call(error, 'terminal')) {
        this.options.appState.setNonIdxError(error);
      } else {
        await this.showFormErrors(model, error, this.formView.form);
      }
    } finally {
      this.toggleFormButtonState(false);
    }
  },
  transformIdentifier: function (formName, model) {
    const modelJSON = model.toJSON();

    if (Object.prototype.hasOwnProperty.call(modelJSON, 'identifier')) {
      // The callback function is passed two arguments:
      // 1) username: The name entered by the user
      // 2) operation: The type of operation the user is trying to perform:
      //      - PRIMARY_AUTH
      //      - FORGOT_PASSWORD
      //      - UNLOCK_ACCOUNT
      const operation = FORM_NAME_TO_OPERATION_MAP[formName];
      modelJSON.identifier = this.settings.transformUsername(modelJSON.identifier, operation);
    }

    return modelJSON;
  },

  /**
   * @param model current form model
   * @param error any errors after user action
   * @param form current form
   * Handle errors that get displayed right after any user action. After such form errors widget doesn't
   * reload or re-render, but updates the AppSate with latest remediation.
   */
  showFormErrors: async function (model, error, form) {
    var _error, _idxStateError, _idxStateError2;

    /* eslint max-statements: [2, 24] */
    let errorObj;
    let idxStateError;
    let showErrorBanner = true;
    model.trigger('clearFormError');

    if (!error) {
      error = 'FormController - unknown error found';
      this.options.settings.callGlobalError(error);
    }

    if ((_error = error) !== null && _error !== void 0 && _error.rawIdxState) {
      idxStateError = error;
      error = error.rawIdxState;
    }

    if (IonResponseHelper.isIonErrorResponse(error)) {
      errorObj = IonResponseHelper.convertFormErrors(error);
    } else if (error.errorSummary) {
      errorObj = {
        responseJSON: error
      };
    } else {
      Util.logConsoleError(error);
      errorObj = {
        responseJSON: {
          errorSummary: loc('error.unsupported.response', 'login')
        }
      };
    }

    if (oktaUnderscore.isFunction(form === null || form === void 0 ? void 0 : form.showCustomFormErrorCallout)) {
      showErrorBanner = !form.showCustomFormErrorCallout(errorObj, idxStateError.messages);
    } // show error before updating app state.


    model.trigger('error', model, errorObj, showErrorBanner);
    idxStateError = Object.assign({}, idxStateError, {
      hasFormError: true
    }); // TODO OKTA-408410: Widget should update the state on every new response. It should NOT do selective update.
    // For eg 429 rate-limit errors, we have to skip updating idx state, because error response is not an idx response.

    if (Array.isArray((_idxStateError = idxStateError) === null || _idxStateError === void 0 ? void 0 : _idxStateError.neededToProceed) && (_idxStateError2 = idxStateError) !== null && _idxStateError2 !== void 0 && _idxStateError2.neededToProceed.length) {
      await this.handleIdxResponse(idxStateError);
    }
  },
  handleIdxResponse: async function (idxResp) {
    await updateAppState(this.options.appState, idxResp);
  },

  /**
   * SignIn widget has its own (hacky) way to customize the button disabled state:
   * adding `link-button-disabled` despite the name was intend only to disable
   * `link-button`.
   * Instead of doing decent refactor, we want to follow the convention for now.
   *
   * @param {boolean} disabled whether add extra disable CSS class.
   */
  toggleFormButtonState: function (disabled) {
    const button = this.$el.find('.o-form-button-bar .button');
    button.toggleClass('link-button-disabled', disabled);
  }
});

export { FormController as default };
//# sourceMappingURL=FormController.js.map
