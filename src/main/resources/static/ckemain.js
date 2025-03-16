const {
    ClassicEditor,
    Alignment,
    Autoformat,
    AutoImage,
    Autosave,
    BlockQuote,
    Bold,
    Emoji,
    Essentials,
    FontBackgroundColor,
    FontColor,
    FontFamily,
    FontSize,
    GeneralHtmlSupport,
    HorizontalLine,
    HtmlComment,
    HtmlEmbed,
    ImageBlock,
    ImageCaption,
    ImageInline,
    ImageInsert,
    ImageInsertViaUrl,
    ImageResize,
    ImageStyle,
    ImageTextAlternative,
    ImageToolbar,
    ImageUpload,
    MediaEmbed,
    Mention,
    Paragraph,
    PasteFromOffice,
    SimpleUploadAdapter,
    SourceEditing,
    Strikethrough,
    Table,
    TableCaption,
    TableCellProperties,
    TableColumnResize,
    TableProperties,
    TableToolbar,
    TextTransformation,
    Underline
} = window.CKEDITOR;

const LICENSE_KEY =
    'eyJhbGciOiJFUzI1NiJ9.eyJleHAiOjE3NzI5Mjc5OTksImp0aSI6IjEwNTMxNzkxLTkwZWMtNGI0OC1iN2E5LWYwZTczZDM0MWZkMiIsInVzYWdlRW5kcG9pbnQiOiJodHRwczovL3Byb3h5LWV2ZW50LmNrZWRpdG9yLmNvbSIsImRpc3RyaWJ1dGlvbkNoYW5uZWwiOlsiY2xvdWQiLCJkcnVwYWwiXSwiZmVhdHVyZXMiOlsiRFJVUCJdLCJ2YyI6IjFkYjNlNjM1In0.pJTr4E8IlympucIdFZ6fbByFYCcfVfLWFRQreIKxekq12opiepD2OjgknFPdKKlDwEi7Iz166-ymeoTb5QE0HQ';

const editorConfig = {
    toolbar: {
        items: [
            'sourceEditing',
            '|',
            'fontSize',
            'fontFamily',
            'fontColor',
            'fontBackgroundColor',
            '|',
            'bold',
            'underline',
            'strikethrough',
            '|',
            'emoji',
            'horizontalLine',
            'insertImage',
            'mediaEmbed',
            'insertTable',
            'blockQuote',
            'htmlEmbed',
            '|',
            'alignment'
        ],
        shouldNotGroupWhenFull: false
    },
    plugins: [
        Alignment,
        Autoformat,
        AutoImage,
        Autosave,
        BlockQuote,
        Bold,
        Emoji,
        Essentials,
        FontBackgroundColor,
        FontColor,
        FontFamily,
        FontSize,
        GeneralHtmlSupport,
        HorizontalLine,
        HtmlComment,
        HtmlEmbed,
        ImageBlock,
        ImageCaption,
        ImageInline,
        ImageInsert,
        ImageInsertViaUrl,
        ImageResize,
        ImageStyle,
        ImageTextAlternative,
        ImageToolbar,
        ImageUpload,
        MediaEmbed,
        Mention,
        Paragraph,
        PasteFromOffice,
        SimpleUploadAdapter,
        SourceEditing,
        Strikethrough,
        Table,
        TableCaption,
        TableCellProperties,
        TableColumnResize,
        TableProperties,
        TableToolbar,
        TextTransformation,
        Underline
    ],
    fontFamily: {
        supportAllValues: true
    },
    fontSize: {
        options: [10, 12, 14, 'default', 18, 20, 22],
        supportAllValues: true
    },
    htmlSupport: {
        allow: [
            {
                name: /^.*$/,
                styles: true,
                attributes: true,
                classes: true
            }
        ]
    },
    image: {
        toolbar: [
            'toggleImageCaption',
            'imageTextAlternative',
            '|',
            'imageStyle:inline',
            'imageStyle:wrapText',
            'imageStyle:breakText',
            '|',
            'resizeImage'
        ]
    },
    initialData:
        "<h2>Congratulations on setting up CKEditor 5! 🎉</h2>\n<p>\n\tYou've successfully created a CKEditor 5 project. This powerful text editor\n\twill enhance your application, enabling rich text editing capabilities that\n\tare customizable and easy to use.\n</p>\n<h3>What's next?</h3>\n<ol>\n\t<li>\n\t\t<strong>Integrate into your app</strong>: time to bring the editing into\n\t\tyour application. Take the code you created and add to your application.\n\t</li>\n\t<li>\n\t\t<strong>Explore features:</strong> Experiment with different plugins and\n\t\ttoolbar options to discover what works best for your needs.\n\t</li>\n\t<li>\n\t\t<strong>Customize your editor:</strong> Tailor the editor's\n\t\tconfiguration to match your application's style and requirements. Or\n\t\teven write your plugin!\n\t</li>\n</ol>\n<p>\n\tKeep experimenting, and don't hesitate to push the boundaries of what you\n\tcan achieve with CKEditor 5. Your feedback is invaluable to us as we strive\n\tto improve and evolve. Happy editing!\n</p>\n<h3>Helpful resources</h3>\n<p>\n\t<i>An editor without the </i><code>Link</code>\n\t<i>plugin? That's brave! We hope the links below will be useful anyway </i>😉\n</p>\n<ul>\n\t<li>📝 Trial sign up: https://portal.ckeditor.com/checkout?plan=free,</li>\n\t<li>📕 Documentation: https://ckeditor.com/docs/ckeditor5/latest/installation/index.html,</li>\n\t<li>⭐️ GitHub (star us if you can!): https://github.com/ckeditor/ckeditor5,</li>\n\t<li>🏠 CKEditor Homepage: https://ckeditor.com,</li>\n\t<li>🧑‍💻 CKEditor 5 Demos: https://ckeditor.com/ckeditor-5/demo/</li>\n</ul>\n<h3>Need help?</h3>\n<p>\n\tSee this text, but the editor is not starting up? Check the browser's\n\tconsole for clues and guidance. It may be related to an incorrect license\n\tkey if you use premium features or another feature-related requirement. If\n\tyou cannot make it work, file a GitHub issue, and we will help as soon as\n\tpossible!\n</p>\n",
    language: 'ko',
    licenseKey: LICENSE_KEY,
    placeholder: 'Type or paste your content here!',
    table: {
        contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
    }
};

class MyUploadAdapter {
    constructor(loader) {
        this.loader = loader;
    }
    upload() {
        return this.loader.file.then(file => new Promise((resolve, reject) => {
            const data = new FormData();
            data.append('upload', file);

            // 만약 백엔드에서 파일명을 별도로 처리하지 않는다면, 어댑터는 단순히 파일을 전송합니다.
            // 여기서는 파일명은 백엔드(AjeGagController.uploadFile)에서 UUID를 추가하도록 되어 있으므로 그대로 전송합니다.

            fetch('/ajeGag/uploadFile', {
                method: 'POST',
                body: data
            })
                .then(response => response.json())
                .then(result => {
                    // CKEditor는 { default: "이미지 URL" } 형태의 응답을 요구합니다.
                    resolve({ default: result.url });
                })
                .catch(error => {
                    console.error('업로드 실패:', error);
                    reject('Upload failed');
                });
        }));
    }
    abort() {
        // 필요시 업로드 중단 기능 구현
    }
}

ClassicEditor.create(document.querySelector('#adminDetail'), editorConfig)
    .then(editor => {
        // 기존 MyUploadAdapter를 통한 이미지 업로드 설정
        editor.plugins.get('FileRepository').createUploadAdapter = loader => new MyUploadAdapter(loader);
        window.editor = editor;
    })
    .catch(error => {
        console.error(error);
    });