module.exports = {
  extends: [
    'stylelint-prettier/recommended',
    'stylelint-config-recommended-scss',
    'stylelint-config-recommended-vue/scss',
  ],
  // add your custom config here
  // https://stylelint.io/user-guide/configuration
  rules: {
    'selector-pseudo-element-no-unknown': [
      true,
      {
        ignorePseudoElements: ['v-deep'],
      },
    ],
  },
}
