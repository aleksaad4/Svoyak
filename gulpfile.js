'use strict';

// https://habrahabr.ru/post/252745/

var gulp = require('gulp'),
//  construct pipes of streams of events (eventStream is like functional programming meets IO)
    es = require('event-stream'),
//  only pass through changed files
    changed = require('gulp-changed'),
//  live CSS Reload &amp; Browser Syncing
    browserSync = require('browser-sync'),//.create(),
//  conditionally run a task
    gulpIf = require('gulp-if'),

//  concatenates files
    concat = require('gulp-concat'),
//  static asset revisioning by appending content hash to filenames: unicorn.css =&gt; unicorn-d41d8cd98f.css
    rev = require('gulp-rev'),
    revreplace = require("gulp-rev-replace"),

//  remove or replace relative path for files
    flatten = require('gulp-flatten'),
//  a gulp plugin for removing files and folders
    clean = require('gulp-clean'),
//  a string replace plugin for gulp
    replace = require('gulp-replace'),
//  source map support for Gulp.js
    sourcemaps = require('gulp-sourcemaps'),

//  gulp plugin for sass
    sass = require('gulp-sass'),
//  minify css with clean-css
    csso = require('gulp-csso'),
//  gulp plugin to minify HTML
    htmlmin = require('gulp-htmlmin'),
//  replaces references to non-optimized scripts or stylesheets into a set of HTML files (or any templates/views)
    usemin = require('gulp-usemin'),
//  minify files with UglifyJS
    uglify = require('gulp-uglify'),
//  minify PNG, JPEG, GIF and SVG images
    imagemin = require('gulp-imagemin'),
//  a gulp plugin for using rigger ("include" other files into, //= footer.html)
    rigger = require('gulp-rigger'),
// less
    less = require('gulp-less'),
//  prefix CSS with Autoprefixer
    prefix = require('gulp-autoprefixer'),


//  JSHint plugin for gulp (JSHint is a tool that helps to detect errors and potential problems in your JavaScript code)
    jshint = require('gulp-jshint'),
    htmlhint = require('gulp-htmlhint'),
    csslint = require('gulp-csslint');


var isDev = true;


var targetSource = 'build/webapp/';
var targetResources = 'build/resources/main/static/';
var webapp = 'src/main/webapp/';
var sassPattern = '/**/*.{scss,sass}';
var lessPattern = '/**/*.less';
var cssPattern = '/**/*.css';
var jsPattern = '/**/*.js';
var imagesPattern = '/**/*';
var htmlPattern = '/**/*.html';
var bowerDir = webapp + 'bower_components/';
var styles = 'styles.css';
var js = 'script.js';

function getApp(appName) {
    return {
        css: appName + 'css/',
        sass: appName + 'scss/',
        less: appName + 'less/',
        fonts: appName + 'fonts/',
        images: appName + 'images/',
        html: appName + 'html/',
        js: appName + 'js/',
        compiledCss: appName + 'compiledCss/',
        base: appName
    }
}

var app = getApp('app/game/');

// clean task - очищаем папку webapp в target
gulp.task('clean', function () {
    return gulp
    // можно не читать содержимое папки, нам всё равно щас её просто удалять
        .src([targetSource, targetResources], {read: false})
        // удаляем
        .pipe(clean());
});

// task для сборки sass стилей
gulp.task('sass', function () {
    return es.merge(
        // берём всё sass файлы в папке app.sass
        gulp.src(webapp + app.sass + sassPattern)
            //  будем заменять только те файлы, которые изменятся
            .pipe(changed(targetResources + app.compiledCss, {extension: '.css'}))
            // компилируем sass файлы
            .pipe(sass({includePaths: bowerDir}).on('error', sass.logError))
            // сохраняем их в cssDir
            .pipe(gulp.dest(targetResources + app.compiledCss)),
        // скопируем font-ы из bower-а
        gulp.src(bowerDir + '**/fonts/**/*.{woff,woff2,svg,ttf,eot,otf}')
            // будем заменять только те файлы, которые изменяются
            .pipe(changed(targetResources + app.fonts))
            // исправляем относительные пути
            .pipe(flatten())
            // сохраняем шрифты в fonts
            .pipe(gulp.dest(targetResources + app.fonts))
    );
});

// task для сборки less-файлов
gulp.task('less', function () {
    return gulp.src(webapp + app.less + lessPattern)
        .pipe(less())
        .pipe(gulp.dest(targetResources + app.compiledCss));
});

// таск для копирования шрифтов
gulp.task('fonts', function () {
    return gulp.src(webapp + app.fonts)
        .pipe(gulp.dest(webapp + app.fonts));
});

// таск для подготовки стилей
gulp.task('styles', ['sass', 'less'], function () {
    return gulp.src([webapp + app.css + cssPattern, targetResources + app.compiledCss + cssPattern])
        .pipe(concat(styles, {newLine: '\n'}))
        .pipe(prefix())
        .pipe(gulpIf(!isDev, csso()))
        .pipe(csslint())
        .pipe(csslint.reporter())
        .pipe(gulpIf(!isDev, rev()))
        .pipe(gulp.dest(targetResources + app.css))
        .pipe(gulpIf(!isDev, rev.manifest({merge: true})))
        .pipe(gulp.dest(targetResources + app.base))
        .pipe(browserSync.reload({stream: true}));
});

// таск для подготовки html файлов
gulp.task('html', function () {
    // берём все наши html шаблоны
    return gulp.src(webapp + app.html + htmlPattern)
        // минимизируем html файлы
        .pipe(gulpIf(!isDev, htmlmin({collapseWhitespace: true})))
        .pipe(htmlhint()).pipe(htmlhint.reporter())
        .pipe(gulpIf(!isDev, revreplace({manifest: gulp.src(targetResources + app.base + "rev-manifest.json")})))
        .pipe(gulp.dest(targetSource + app.html));
});

// обработаем все скрипты и склеим их в два файла - свой и чужой
gulp.task('scripts', function () {
    return gulp.src(webapp + app.js + jsPattern)
        .pipe(concat(js, {newLine: '\n'}))
        .pipe(jshint())
        .pipe(jshint.reporter())
        .pipe(gulpIf(!isDev, uglify()))
        .pipe(gulpIf(!isDev, rev()))
        .pipe(gulp.dest(targetResources + app.js))
        .pipe(gulpIf(!isDev, rev.manifest({merge: true})))
        .pipe(gulp.dest(targetResources + app.base));
});

// таск по обработке картинок
gulp.task('images', function () {
    // все из папки images
    return gulp.src(webapp + app.images + imagesPattern)
        // только те файлы, которые изменились
        .pipe(changed(targetSource + app.images))
        // минимизируем картинки
        .pipe(imagemin({optimizationLevel: 5, progressive: true, interlaced: true}))
        // проставляем ревизию
        .pipe(gulpIf(!isDev, rev()))
        // копируем в target
        .pipe(gulp.dest(targetSource + app.images))
        .pipe(gulpIf(!isDev, rev.manifest({merge: true})))
        .pipe(gulp.dest(targetResources + app.base));
    // кидаем sync
    //.pipe(browserSync.reload());
});


gulp.task('serve', function () {

    // browserSync.init({
    //     open: true,
    //     port: 8080,
    //     server: {
    //         baseDir: targetSource
    //     }
    // });

  gulp.watch(webapp + app.css + cssPattern, ['styles']);

    //gulp.watch(webapp + app.less + lessPattern, ['styles']);
    //gulp.watch(webapp + app.sass + sassPattern, ['styles']);
    // gulp.watch(webapp + app.css + cssPattern, ['sass']);
    // gulp.watch(webapp + app.images + imagesPattern, ['images']);
    // gulp.watch(webapp + app.js + jsPattern, ['scripts']);
    // gulp.watch(webapp + app.html + htmlPattern, ['html']).on('change', browserSync.reload);
});

// основной таск для сборки (по умолчанию dev режим)
gulp.task('build', ['less', 'sass', 'styles', 'fonts', 'scripts', 'images', 'html']);

// таск для сборки в dev режиме
gulp.task('dev', ['build']);

// таск для сборки в prod режиме
gulp.task('prod', function () {
    // скидываем флаг
    isDev = false;
    // вызываем основной таск
    gulp.start('build');
});
