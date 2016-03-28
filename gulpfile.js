'use strict';

var gulp = require('gulp'),
// This plugin is intended for testing other gulp plugin
    expect = require('gulp-expect-file'),
// Gulp plugin for sass
    sass = require('gulp-sass'),
// Static asset revisioning by appending content hash to filenames: unicorn.css =&gt; unicorn-d41d8cd98f.css
    rev = require('gulp-rev'),
// Concatenates and registers AngularJS templates in the $templateCache
    templateCache = require('gulp-angular-templatecache'),
// gulp plugin to minify HTML
    htmlmin = require('gulp-htmlmin'),
// Minify PNG, JPEG, GIF and SVG images
    imagemin = require('gulp-imagemin'),
// A gulp plugin for processing files with ESLint
    eslint = require('gulp-eslint'),
// construct pipes of streams of events
    es = require('event-stream'),
// remove or replace relative path for files
    flatten = require('gulp-flatten'),
// Delete files/folders using globs
    del = require('del'),
// Wire Bower dependencies to your source code
    wiredep = require('wiredep').stream,
// Run a series of dependent gulp tasks in order
    runSequence = require('run-sequence'),
// Live CSS Reload &amp; Browser Syncing
    browserSync = require('browser-sync'),
// Prevent pipe breaking caused by errors from gulp plugins
    plumber = require('gulp-plumber'),
// Only pass through changed files
    changed = require('gulp-changed'),
// Conditionally run a task
    gulpIf = require('gulp-if'),
// A javascript, stylesheet and webcomponent injection plugin for Gulp, i.e. inject file references into your index.html
    inject = require('gulp-inject'),
// Automatically sort AngularJS app files depending on module definitions and usage
    angularFilesort = require('gulp-angular-filesort');

var handleErrors = require('./gulp/handleErrors'),
    serve = require('./gulp/serve'),
    util = require('./gulp/utils'),
    build = require('./gulp/build');

var config = require('./gulp/config');

gulp.task('clean', function () {
    return del([config.dist], {dot: true});
});

gulp.task('copy', function () {
    return es.merge(
        gulp.src(config.app + 'content/**/*.{woff,woff2,svg,ttf,eot,otf}')
            .pipe(plumber({errorHandler: handleErrors}))
            .pipe(changed(config.dist + 'content/fonts/'))
            .pipe(flatten())
            .pipe(rev())
            .pipe(gulp.dest(config.dist + 'content/fonts/'))
            .pipe(rev.manifest(config.revManifest, {
                base: config.dist,
                merge: true
            }))
            .pipe(gulp.dest(config.dist)),
        gulp.src([config.app + 'robots.txt', config.app + 'favicon.ico', config.app + '.htaccess'], {dot: true})
            .pipe(plumber({errorHandler: handleErrors}))
            .pipe(changed(config.dist))
            .pipe(gulp.dest(config.dist))
    );
});

gulp.task('images', function () {
    return gulp.src(config.app + 'content/images/**')
        .pipe(plumber({errorHandler: handleErrors}))
        .pipe(changed(config.dist + 'content/images'))
        .pipe(imagemin({optimizationLevel: 5, progressive: true, interlaced: true}))
        .pipe(rev())
        .pipe(gulp.dest(config.dist + 'content/images'))
        .pipe(rev.manifest(config.revManifest, {
            base: config.dist,
            merge: true
        }))
        .pipe(gulp.dest(config.dist))
        .pipe(browserSync.reload({stream: true}));
});

gulp.task('sass', function () {
    return es.merge(
        gulp.src(config.sassSrc)
            .pipe(plumber({errorHandler: handleErrors}))
            .pipe(expect(config.sassSrc))
            .pipe(changed(config.cssDir, {extension: '.css'}))
            .pipe(sass({includePaths: config.bower}).on('error', sass.logError))
            .pipe(gulp.dest(config.cssDir)),
        gulp.src(config.bower + '**/fonts/**/*.{woff,woff2,svg,ttf,eot,otf}')
            .pipe(plumber({errorHandler: handleErrors}))
            .pipe(changed(config.app + 'content/fonts'))
            .pipe(flatten())
            .pipe(gulp.dest(config.app + 'content/fonts'))
    );
});


gulp.task('styles', ['sass'], function () {
    return gulp.src(config.app + 'content/css')
        .pipe(browserSync.reload({stream: true}));
});

gulp.task('inject', function () {
    return gulp.src(config.app + 'index.html')
        .pipe(inject(gulp.src(config.app + 'app/**/*.js').pipe(angularFilesort()), {relative: true}))
        .pipe(gulp.dest(config.app));
});

gulp.task('wiredep', ['wiredep:app']);

gulp.task('wiredep:app', function () {
    var stream = gulp.src(config.app + 'index.html')
        .pipe(plumber({errorHandler: handleErrors}))
        .pipe(wiredep({
            exclude: [
                /angular-i18n/,  // localizations are loaded dynamically
                'bower_components/bootstrap-sass/assets/javascripts/' // Exclude Bootstrap js files as we use ui-bootstrap
            ]
        }))
        .pipe(gulp.dest(config.app));

    return es.merge(stream, gulp.src(config.sassSrc)
        .pipe(plumber({errorHandler: handleErrors}))
        .pipe(wiredep({
            ignorePath: /\.\.\/webapp\/bower_components\// // remove ../webapp/bower_components/ from paths of injected sass files
        }))
        .pipe(gulp.dest(config.scss)));
});

gulp.task('assets:prod', ['images', 'styles', 'html'], build);

gulp.task('html', function () {
    return gulp.src(config.app + 'app/**/*.html')
        .pipe(htmlmin({collapseWhitespace: true}))
        .pipe(templateCache({
            module: 'jhipsterTestApp',
            root: 'app/',
            moduleSystem: 'IIFE'
        }))
        .pipe(gulp.dest(config.tmp));
});


// check app for eslint errors
gulp.task('eslint', function () {
    return gulp.src(['gulpfile.js', config.app + 'app/**/*.js'])
        .pipe(plumber({errorHandler: handleErrors}))
        .pipe(eslint())
        .pipe(eslint.format())
        .pipe(eslint.failOnError());
});

// check app for eslint errors anf fix some of them
gulp.task('eslint:fix', function () {
    return gulp.src(config.app + 'app/**/*.js')
        .pipe(plumber({errorHandler: handleErrors}))
        .pipe(eslint({
            fix: true
        }))
        .pipe(eslint.format())
        .pipe(gulpIf(util.isLintFixed, gulp.dest(config.app + 'app')));
});

gulp.task('watch', function () {
    gulp.watch('bower.json', ['install']);
    gulp.watch(['gulpfile.js', 'build.gradle'], ['ngconstant:dev']);
    gulp.watch(config.sassSrc, ['styles']);
    gulp.watch(config.app + 'content/images/**', ['images']);
    gulp.watch(config.app + 'app/**/*.js', ['inject']);
    gulp.watch([config.app + '*.html', config.app + 'app/**', config.app + 'i18n/**']).on('change', browserSync.reload);
});

gulp.task('install', function () {
    runSequence(['wiredep', 'ngconstant:dev'], 'sass', 'inject');
});

gulp.task('serve', function () {
    runSequence('install', serve);
});

gulp.task('build', ['clean'], function (cb) {
    runSequence(['copy', 'wiredep:app', 'ngconstant:prod'], 'inject', 'assets:prod', cb);
});

gulp.task('default', ['serve']);
