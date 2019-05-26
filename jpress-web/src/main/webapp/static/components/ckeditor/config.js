/**
 * @license Copyright (c) 2003-2018, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see https://ckeditor.com/legal/ckeditor-oss-license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here. For example:
	// config.language = 'fr';
	// config.uiColor = '#AADC6E';

    // 去掉复制样式
    // CKEDITOR.config.pasteFromWordRemoveStyles = true;
    // CKEDITOR.config.forcePasteAsPlainText = true;
    config.pasteFromWordRemoveStyles = true;
    config.forcePasteAsPlainText = true;

    config.fontSize_defaultLabel = '18px';
};
