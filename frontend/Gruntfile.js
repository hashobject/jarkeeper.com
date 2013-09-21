module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    stylus: {
      compile: {
        options: {
          paths: ['styl'], // folder, where files to be imported are located
          urlfunc: 'url',
          'include css': true
        },
        files: {
          '../resources/public/app.css': 'styl/index.styl' // 1:1 compile
        }
      }
    },


    watch: {
      src: {
        files: ['styl/*.styl'],
        tasks: ['build']
      }
    }

  });

  grunt.loadNpmTasks('grunt-contrib-stylus');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-watch');

  grunt.registerTask('build', ['stylus:compile']);
  //grunt.registerTask('deploy', ['stylus:compile']);

};
