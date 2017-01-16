" Vim syntax file
" Language: REPLesent
" Latest Revision: 2016-05-17
" Depends on: https://github.com/derekwyatt/vim-scala

syntax include @Scala syntax/scala.vim
syntax region replesentBlock start=/```/ end=/```/ contains=@Scala
