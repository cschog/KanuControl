import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn from '../../../util/Animations.js';

function setBackgroundImage(el, appState) {
  const imgSrc = appState.get('securityImage');
  const imgDescription = appState.get('securityImageDescription');
  const isUndefinedUser = appState.get('isUndefinedUser');
  const isNewUser = appState.get('isNewUser');
  const isSecurityImage = !isUndefinedUser && !isNewUser; // NOTE: The imgSrc is returned by the server so that every
  // user has a unique image. However new and undefined user states
  // are hard coded into the css and the value returned by the server
  // is ignored.

  el.css('background-image', '');
  el.removeClass('new-user undefined-user');

  if (isNewUser) {
    el.addClass('new-user');
    return;
  }

  if (isUndefinedUser) {
    el.addClass('undefined-user');
    return;
  }

  if (isSecurityImage) {
    // TODO: Newer versions of qtip will remove aria-describedby on their own when destroy() is called.
    el.removeAttr('aria-describedby');
    el.find('.accessibility-text').text(imgDescription);
    el.css('background-image', 'url(' + oktaUnderscore.escape(imgSrc) + ')');
    return;
  }
}

function antiPhishingMessage(image, host) {
  oktaJQueryStatic(window).on('resize.securityBeaconQtip', oktaUnderscore.debounce(function () {
    if (image.is(':visible')) {
      image.qtip('show');
    }
  }, 300)); // Show the message that the user has not logged in from this device before.

  image.qtip({
    prerender: true,
    content: {
      text: loc('primaryauth.newUser.tooltip', 'login', [oktaUnderscore.escape(host)]),
      button: loc('primaryauth.newUser.tooltip.close', 'login')
    },
    style: {
      classes: 'okta-security-image-tooltip security-image-qtip qtip-custom qtip-shadow qtip-rounded',
      tip: {
        height: 12,
        width: 16
      }
    },
    position: {
      my: 'top center',
      at: 'center',
      target: oktaJQueryStatic('.auth-beacon-security'),
      adjust: {
        method: 'flip',
        scroll: false,
        resize: true
      },
      effect: false
    },
    hide: {
      event: false,
      fixed: true
    },
    show: {
      event: false,
      delay: 200
    },
    events: {
      move: function (event, api) {
        if (!api.elements.target.is(':visible')) {
          // Have to hide it immediately, with no effect
          api.set('hide.effect', false);
          api.hide();
          api.set('hide.effect', true);
        }
      }
    }
  }); // It is necessary to delay toggle to the next render cycle, since qtip internally defers some setup tasks.

  setTimeout(() => {
    image.qtip('toggle', image.is(':visible'));
  }, 0);
}

function destroyAntiPhishingMessage(image) {
  image.qtip('destroy');
  oktaJQueryStatic(window).off('resize.securityBeaconQtip');
}

async function updateSecurityImage($el, appState, animate) {
  const image = $el.find('.auth-beacon-security');
  const border = $el.find('.js-auth-beacon-border');
  const hasBorder = !appState.get('isUndefinedUser');
  const hasAntiPhishing = appState.get('isNewUser');
  const radialProgressBar = $el.find('.radial-progress-bar');
  const host = appState.get('baseUrl').match(/https?:\/\/(.[^/]+)/)[1];
  const duration = 200;

  if (!animate) {
    // Do not animate the security beacon
    // This occurs when initializing the form
    setBackgroundImage(image, appState);
    border.toggleClass('auth-beacon-border', hasBorder);
    return;
  }

  destroyAntiPhishingMessage(image); // Animate loading the security beacon

  if (!hasBorder) {
    // This occurs when appState username is blank
    // we do not yet know if the user is recognized
    image.fadeOut(duration, function () {
      setBackgroundImage(image, appState);
      border.removeClass('auth-beacon-border');
      image.fadeIn(duration);
    });
  } else {
    // Animate loading the security beacon with a loading bar for the border
    // This occurs when the username has been checked against Okta.
    border.removeClass('auth-beacon-border');
    await fn.radialProgressBar({
      $el: radialProgressBar,
      swap: function () {
        image.fadeOut(duration, () => {
          setBackgroundImage(image, appState);
          image.fadeIn(duration);
        });
      }
    });
    border.addClass('auth-beacon-border');

    if (hasAntiPhishing) {
      antiPhishingMessage(image, host);
    }
  }
}

var SecurityBeacon = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      return "<div class=\"beacon-blank\"><div class=\"radial-progress-bar\"><div class=\"circle left\"></div><div class=\"circle right\"></div></div></div><div aria-live=\"polite\" role=\"img\" class=\"bg-helper auth-beacon auth-beacon-security\" data-se=\"security-beacon\"><span class=\"accessibility-text\"></span><div class=\"okta-sign-in-beacon-border auth-beacon-border js-auth-beacon-border\"></div></div>";
    },
    "useData": true
  }),
  className: 'js-security-beacon',
  initialize: function (options) {
    this.update = oktaUnderscore.partial(updateSecurityImage, this.$el, options.appState);
    this.listenTo(options.appState, 'change:securityImage', this.update);
    this.listenTo(options.appState, 'loading', function (isLoading) {
      this.$el.toggleClass('beacon-loading', isLoading);
      this.removeAntiPhishingMessage();
    });
    this.options.appState.set('beaconType', 'security');
    this.listenTo(options.appState, 'navigate', this.removeAntiPhishingMessage);
  },
  postRender: function () {
    this.update(false);
  },
  equals: function (Beacon) {
    return Beacon && this instanceof Beacon;
  },
  removeAntiPhishingMessage: function () {
    const image = this.$el.find('.auth-beacon-security');
    image.qtip('destroy');
  }
});

export { SecurityBeacon as default };
//# sourceMappingURL=SecurityBeacon.js.map
