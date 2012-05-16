/* The following code was generated by JFlex 1.4.3 on 27/07/11 14:49 */

/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jsyntaxpane.lexers;


import jsyntaxpane.Token;
import jsyntaxpane.TokenType;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.3
 * on 27/07/11 14:49 from the specification file
 * <tt>/mnt/data/Code/java/praxis/jsyntaxpane/praxis-jsyntaxpane/src/main/jflex/jsyntaxpane/lexers/lua.flex</tt>
 */
public final class LuaLexer extends DefaultJFlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int COMMENT = 8;
  public static final int LONGSTRING = 6;
  public static final int STRING2 = 4;
  public static final int LINECOMMENT = 10;
  public static final int STRING1 = 2;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5, 5
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\10\1\3\1\2\1\0\1\3\1\1\16\10\4\0\1\3\1\0"+
    "\1\20\1\46\1\7\1\46\1\0\1\21\1\51\1\52\1\46\1\45"+
    "\1\46\1\17\1\15\1\46\1\13\11\11\1\46\1\46\1\50\1\5"+
    "\1\50\2\0\4\12\1\16\1\12\24\7\1\4\1\22\1\6\1\46"+
    "\1\7\1\0\1\23\1\26\1\36\1\25\1\30\1\32\1\7\1\42"+
    "\1\34\1\7\1\31\1\35\1\7\1\24\1\33\1\44\1\7\1\27"+
    "\1\43\1\37\1\40\1\7\1\41\1\14\2\7\1\53\1\0\1\54"+
    "\1\47\41\10\2\0\4\7\4\0\1\7\2\0\1\10\7\0\1\7"+
    "\4\0\1\7\5\0\27\7\1\0\37\7\1\0\u013f\7\31\0\162\7"+
    "\4\0\14\7\16\0\5\7\11\0\1\7\21\0\130\10\5\0\23\10"+
    "\12\0\1\7\13\0\1\7\1\0\3\7\1\0\1\7\1\0\24\7"+
    "\1\0\54\7\1\0\46\7\1\0\5\7\4\0\202\7\1\0\4\10"+
    "\3\0\105\7\1\0\46\7\2\0\2\7\6\0\20\7\41\0\46\7"+
    "\2\0\1\7\7\0\47\7\11\0\21\10\1\0\27\10\1\0\3\10"+
    "\1\0\1\10\1\0\2\10\1\0\1\10\13\0\33\7\5\0\3\7"+
    "\15\0\4\10\14\0\6\10\13\0\32\7\5\0\13\7\16\10\7\0"+
    "\12\10\4\0\2\7\1\10\143\7\1\0\1\7\10\10\1\0\6\10"+
    "\2\7\2\10\1\0\4\10\2\7\12\10\3\7\2\0\1\7\17\0"+
    "\1\10\1\7\1\10\36\7\33\10\2\0\3\7\60\0\46\7\13\10"+
    "\1\7\u014f\0\3\10\66\7\2\0\1\10\1\7\20\10\2\0\1\7"+
    "\4\10\3\0\12\7\2\10\2\0\12\10\21\0\3\10\1\0\10\7"+
    "\2\0\2\7\2\0\26\7\1\0\7\7\1\0\1\7\3\0\4\7"+
    "\2\0\1\10\1\7\7\10\2\0\2\10\2\0\3\10\11\0\1\10"+
    "\4\0\2\7\1\0\3\7\2\10\2\0\12\10\4\7\15\0\3\10"+
    "\1\0\6\7\4\0\2\7\2\0\26\7\1\0\7\7\1\0\2\7"+
    "\1\0\2\7\1\0\2\7\2\0\1\10\1\0\5\10\4\0\2\10"+
    "\2\0\3\10\13\0\4\7\1\0\1\7\7\0\14\10\3\7\14\0"+
    "\3\10\1\0\11\7\1\0\3\7\1\0\26\7\1\0\7\7\1\0"+
    "\2\7\1\0\5\7\2\0\1\10\1\7\10\10\1\0\3\10\1\0"+
    "\3\10\2\0\1\7\17\0\2\7\2\10\2\0\12\10\1\0\1\7"+
    "\17\0\3\10\1\0\10\7\2\0\2\7\2\0\26\7\1\0\7\7"+
    "\1\0\2\7\1\0\5\7\2\0\1\10\1\7\6\10\3\0\2\10"+
    "\2\0\3\10\10\0\2\10\4\0\2\7\1\0\3\7\4\0\12\10"+
    "\1\0\1\7\20\0\1\10\1\7\1\0\6\7\3\0\3\7\1\0"+
    "\4\7\3\0\2\7\1\0\1\7\1\0\2\7\3\0\2\7\3\0"+
    "\3\7\3\0\10\7\1\0\3\7\4\0\5\10\3\0\3\10\1\0"+
    "\4\10\11\0\1\10\17\0\11\10\11\0\1\7\7\0\3\10\1\0"+
    "\10\7\1\0\3\7\1\0\27\7\1\0\12\7\1\0\5\7\4\0"+
    "\7\10\1\0\3\10\1\0\4\10\7\0\2\10\11\0\2\7\4\0"+
    "\12\10\22\0\2\10\1\0\10\7\1\0\3\7\1\0\27\7\1\0"+
    "\12\7\1\0\5\7\2\0\1\10\1\7\7\10\1\0\3\10\1\0"+
    "\4\10\7\0\2\10\7\0\1\7\1\0\2\7\4\0\12\10\22\0"+
    "\2\10\1\0\10\7\1\0\3\7\1\0\27\7\1\0\20\7\4\0"+
    "\6\10\2\0\3\10\1\0\4\10\11\0\1\10\10\0\2\7\4\0"+
    "\12\10\22\0\2\10\1\0\22\7\3\0\30\7\1\0\11\7\1\0"+
    "\1\7\2\0\7\7\3\0\1\10\4\0\6\10\1\0\1\10\1\0"+
    "\10\10\22\0\2\10\15\0\60\7\1\10\2\7\7\10\4\0\10\7"+
    "\10\10\1\0\12\10\47\0\2\7\1\0\1\7\2\0\2\7\1\0"+
    "\1\7\2\0\1\7\6\0\4\7\1\0\7\7\1\0\3\7\1\0"+
    "\1\7\1\0\1\7\2\0\2\7\1\0\4\7\1\10\2\7\6\10"+
    "\1\0\2\10\1\7\2\0\5\7\1\0\1\7\1\0\6\10\2\0"+
    "\12\10\2\0\2\7\42\0\1\7\27\0\2\10\6\0\12\10\13\0"+
    "\1\10\1\0\1\10\1\0\1\10\4\0\2\10\10\7\1\0\42\7"+
    "\6\0\24\10\1\0\2\10\4\7\4\0\10\10\1\0\44\10\11\0"+
    "\1\10\71\0\42\7\1\0\5\7\1\0\2\7\1\0\7\10\3\0"+
    "\4\10\6\0\12\10\6\0\6\7\4\10\106\0\46\7\12\0\51\7"+
    "\7\0\132\7\5\0\104\7\5\0\122\7\6\0\7\7\1\0\77\7"+
    "\1\0\1\7\1\0\4\7\2\0\7\7\1\0\1\7\1\0\4\7"+
    "\2\0\47\7\1\0\1\7\1\0\4\7\2\0\37\7\1\0\1\7"+
    "\1\0\4\7\2\0\7\7\1\0\1\7\1\0\4\7\2\0\7\7"+
    "\1\0\7\7\1\0\27\7\1\0\37\7\1\0\1\7\1\0\4\7"+
    "\2\0\7\7\1\0\47\7\1\0\23\7\16\0\11\10\56\0\125\7"+
    "\14\0\u026c\7\2\0\10\7\12\0\32\7\5\0\113\7\3\0\3\7"+
    "\17\0\15\7\1\0\4\7\3\10\13\0\22\7\3\10\13\0\22\7"+
    "\2\10\14\0\15\7\1\0\3\7\1\0\2\10\14\0\64\7\40\10"+
    "\3\0\1\7\3\0\2\7\1\10\2\0\12\10\41\0\3\10\2\0"+
    "\12\10\6\0\130\7\10\0\51\7\1\10\126\0\35\7\3\0\14\10"+
    "\4\0\14\10\12\0\12\10\36\7\2\0\5\7\u038b\0\154\7\224\0"+
    "\234\7\4\0\132\7\6\0\26\7\2\0\6\7\2\0\46\7\2\0"+
    "\6\7\2\0\10\7\1\0\1\7\1\0\1\7\1\0\1\7\1\0"+
    "\37\7\2\0\65\7\1\0\7\7\1\0\1\7\3\0\3\7\1\0"+
    "\7\7\3\0\4\7\2\0\6\7\4\0\15\7\5\0\3\7\1\0"+
    "\7\7\17\0\4\10\32\0\5\10\20\0\2\7\23\0\1\7\13\0"+
    "\4\10\6\0\6\10\1\0\1\7\15\0\1\7\40\0\22\7\36\0"+
    "\15\10\4\0\1\10\3\0\6\10\27\0\1\7\4\0\1\7\2\0"+
    "\12\7\1\0\1\7\3\0\5\7\6\0\1\7\1\0\1\7\1\0"+
    "\1\7\1\0\4\7\1\0\3\7\1\0\7\7\3\0\3\7\5\0"+
    "\5\7\26\0\44\7\u0e81\0\3\7\31\0\11\7\6\10\1\0\5\7"+
    "\2\0\5\7\4\0\126\7\2\0\2\10\2\0\3\7\1\0\137\7"+
    "\5\0\50\7\4\0\136\7\21\0\30\7\70\0\20\7\u0200\0\u19b6\7"+
    "\112\0\u51a6\7\132\0\u048d\7\u0773\0\u2ba4\7\u215c\0\u012e\7\2\0\73\7"+
    "\225\0\7\7\14\0\5\7\5\0\1\7\1\10\12\7\1\0\15\7"+
    "\1\0\5\7\1\0\1\7\1\0\2\7\1\0\2\7\1\0\154\7"+
    "\41\0\u016b\7\22\0\100\7\2\0\66\7\50\0\15\7\3\0\20\10"+
    "\20\0\4\10\17\0\2\7\30\0\3\7\31\0\1\7\6\0\5\7"+
    "\1\0\207\7\2\0\1\10\4\0\1\7\13\0\12\10\7\0\32\7"+
    "\4\0\1\7\1\0\32\7\12\0\132\7\3\0\6\7\2\0\6\7"+
    "\2\0\6\7\2\0\3\7\3\0\2\7\3\0\2\7\22\0\3\10"+
    "\4\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\6\0\3\1\1\2\1\3\1\4\1\5\2\6\2\3"+
    "\1\7\1\10\15\5\1\3\1\1\1\11\1\12\1\13"+
    "\1\14\1\15\2\16\1\17\1\1\1\15\1\20\1\21"+
    "\2\15\1\21\1\22\2\23\1\22\2\24\1\25\1\0"+
    "\1\6\2\0\1\3\1\26\3\5\1\27\6\5\1\30"+
    "\5\5\1\31\1\0\1\32\1\33\1\0\1\6\1\0"+
    "\1\6\3\5\1\34\13\5\1\30\6\5\1\35\1\36"+
    "\1\5";

  private static int [] zzUnpackAction() {
    int [] result = new int[111];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\55\0\132\0\207\0\264\0\341\0\u010e\0\u013b"+
    "\0\u0168\0\u0195\0\u01c2\0\u010e\0\u01ef\0\u021c\0\u0249\0\u0276"+
    "\0\u02a3\0\u010e\0\u010e\0\u02d0\0\u02fd\0\u032a\0\u0357\0\u0384"+
    "\0\u03b1\0\u03de\0\u040b\0\u0438\0\u0465\0\u0492\0\u04bf\0\u04ec"+
    "\0\u010e\0\u01c2\0\u010e\0\u010e\0\u010e\0\u010e\0\u0519\0\u0546"+
    "\0\u010e\0\u010e\0\u0573\0\u05a0\0\u010e\0\u010e\0\u05cd\0\u010e"+
    "\0\u05fa\0\u010e\0\u0627\0\u010e\0\u0654\0\u0681\0\u010e\0\u010e"+
    "\0\u0195\0\u06ae\0\u06db\0\u0708\0\u0735\0\u010e\0\u0762\0\u078f"+
    "\0\u07bc\0\u01ef\0\u07e9\0\u0816\0\u0843\0\u0870\0\u089d\0\u08ca"+
    "\0\u01ef\0\u08f7\0\u0924\0\u0951\0\u097e\0\u09ab\0\u010e\0\u05fa"+
    "\0\u010e\0\u010e\0\u0654\0\u09d8\0\u09d8\0\u0708\0\u0a05\0\u0a32"+
    "\0\u0a5f\0\u01ef\0\u0a8c\0\u0ab9\0\u0ae6\0\u0b13\0\u0b40\0\u0b6d"+
    "\0\u0b9a\0\u0bc7\0\u0bf4\0\u0c21\0\u0c4e\0\u0c7b\0\u0ca8\0\u0cd5"+
    "\0\u0d02\0\u0d2f\0\u0d5c\0\u0d89\0\u01ef\0\u01ef\0\u0db6";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[111];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\7\1\10\1\7\1\11\1\12\1\13\1\14\1\15"+
    "\1\7\1\16\1\15\1\17\1\15\1\20\1\15\1\21"+
    "\1\22\1\23\1\7\1\24\1\25\1\26\1\27\1\30"+
    "\1\31\1\15\1\32\1\33\1\34\1\35\1\15\1\36"+
    "\1\37\1\40\3\15\2\41\1\42\1\13\1\43\1\44"+
    "\1\45\1\46\1\47\1\50\1\51\15\47\1\52\1\47"+
    "\1\53\32\47\1\54\1\50\1\51\16\54\1\55\1\53"+
    "\32\54\1\56\1\57\1\60\3\56\1\61\46\56\1\62"+
    "\1\63\1\64\1\62\1\65\50\62\1\56\1\66\1\67"+
    "\52\56\57\0\1\7\55\0\1\11\55\0\1\70\1\71"+
    "\54\0\1\41\56\0\6\15\1\0\1\15\4\0\22\15"+
    "\21\0\1\16\1\0\1\16\1\0\1\72\1\73\11\0"+
    "\1\73\35\0\1\16\1\0\1\16\1\74\1\72\1\73"+
    "\11\0\1\73\35\0\1\72\1\0\1\72\1\0\1\75"+
    "\56\0\1\76\44\0\6\15\1\0\1\15\4\0\1\15"+
    "\1\77\20\15\17\0\6\15\1\0\1\15\4\0\10\15"+
    "\1\100\1\101\10\15\17\0\6\15\1\0\1\15\4\0"+
    "\10\15\1\102\11\15\17\0\6\15\1\0\1\15\4\0"+
    "\4\15\1\103\15\15\17\0\6\15\1\0\1\15\4\0"+
    "\5\15\1\104\14\15\17\0\6\15\1\0\1\15\4\0"+
    "\1\15\1\105\10\15\1\106\7\15\17\0\6\15\1\0"+
    "\1\15\4\0\1\107\7\15\1\33\4\15\1\110\4\15"+
    "\17\0\6\15\1\0\1\15\4\0\4\15\1\111\15\15"+
    "\17\0\6\15\1\0\1\15\4\0\1\15\1\111\5\15"+
    "\1\111\12\15\17\0\6\15\1\0\1\15\4\0\10\15"+
    "\1\112\11\15\17\0\6\15\1\0\1\15\4\0\4\15"+
    "\1\113\12\15\1\114\2\15\17\0\6\15\1\0\1\15"+
    "\4\0\1\15\1\115\20\15\17\0\6\15\1\0\1\15"+
    "\4\0\17\15\1\116\2\15\10\0\1\47\2\0\15\47"+
    "\1\0\1\47\1\0\32\47\2\0\1\51\52\0\2\117"+
    "\1\0\52\117\1\54\2\0\16\54\2\0\32\54\2\0"+
    "\1\60\57\0\1\120\1\121\50\0\1\64\56\0\1\122"+
    "\1\123\51\0\1\67\63\0\1\72\1\0\1\72\2\0"+
    "\1\73\11\0\1\73\35\0\1\124\1\0\1\124\3\0"+
    "\1\125\25\0\1\125\20\0\3\126\2\0\1\126\4\0"+
    "\1\126\1\0\2\126\1\0\1\126\1\0\1\126\3\0"+
    "\1\126\33\0\1\41\46\0\6\15\1\0\1\15\4\0"+
    "\2\15\1\111\17\15\17\0\6\15\1\0\1\15\4\0"+
    "\14\15\1\111\5\15\17\0\6\15\1\0\1\15\4\0"+
    "\12\15\1\111\7\15\17\0\6\15\1\0\1\15\4\0"+
    "\5\15\1\127\14\15\17\0\6\15\1\0\1\15\4\0"+
    "\14\15\1\130\4\15\1\131\17\0\6\15\1\0\1\15"+
    "\4\0\2\15\1\132\17\15\17\0\6\15\1\0\1\15"+
    "\4\0\20\15\1\133\1\15\17\0\6\15\1\0\1\15"+
    "\4\0\12\15\1\134\7\15\17\0\6\15\1\0\1\15"+
    "\4\0\1\15\1\135\20\15\17\0\6\15\1\0\1\15"+
    "\4\0\13\15\1\136\6\15\17\0\6\15\1\0\1\15"+
    "\4\0\15\15\1\137\4\15\17\0\6\15\1\0\1\15"+
    "\4\0\5\15\1\140\14\15\17\0\6\15\1\0\1\15"+
    "\4\0\14\15\1\141\5\15\17\0\6\15\1\0\1\15"+
    "\4\0\11\15\1\142\10\15\21\0\1\124\1\0\1\124"+
    "\50\0\6\15\1\0\1\15\4\0\1\143\21\15\17\0"+
    "\6\15\1\0\1\15\4\0\15\15\1\144\4\15\17\0"+
    "\6\15\1\0\1\15\4\0\5\15\1\145\14\15\17\0"+
    "\6\15\1\0\1\15\4\0\5\15\1\146\14\15\17\0"+
    "\6\15\1\0\1\15\4\0\20\15\1\137\1\15\17\0"+
    "\6\15\1\0\1\15\4\0\13\15\1\147\6\15\17\0"+
    "\6\15\1\0\1\15\4\0\1\101\21\15\17\0\6\15"+
    "\1\0\1\15\4\0\5\15\1\111\14\15\17\0\6\15"+
    "\1\0\1\15\4\0\1\15\1\102\20\15\17\0\6\15"+
    "\1\0\1\15\4\0\11\15\1\150\10\15\17\0\6\15"+
    "\1\0\1\15\4\0\12\15\1\137\7\15\17\0\6\15"+
    "\1\0\1\15\4\0\6\15\1\111\13\15\17\0\6\15"+
    "\1\0\1\15\4\0\4\15\1\151\15\15\17\0\6\15"+
    "\1\0\1\15\4\0\1\152\21\15\17\0\6\15\1\0"+
    "\1\15\4\0\11\15\1\153\10\15\17\0\6\15\1\0"+
    "\1\15\4\0\14\15\1\154\5\15\17\0\6\15\1\0"+
    "\1\15\4\0\12\15\1\155\7\15\17\0\6\15\1\0"+
    "\1\15\4\0\1\15\1\111\20\15\17\0\6\15\1\0"+
    "\1\15\4\0\14\15\1\156\5\15\17\0\6\15\1\0"+
    "\1\15\4\0\7\15\1\111\12\15\17\0\6\15\1\0"+
    "\1\15\4\0\11\15\1\157\10\15\17\0\6\15\1\0"+
    "\1\15\4\0\10\15\1\140\11\15\10\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[3555];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\6\0\1\11\4\1\1\11\5\1\2\11\15\1\1\11"+
    "\1\1\4\11\2\1\2\11\2\1\2\11\1\1\1\11"+
    "\1\1\1\11\1\1\1\11\2\1\2\11\1\0\1\1"+
    "\2\0\1\1\1\11\20\1\1\11\1\0\2\11\1\0"+
    "\1\1\1\0\32\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[111];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public LuaLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

    private static final byte PARAN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte CURLY     = 3;
    private static final byte ENDBLOCK  = 4;
    private static final byte REPEATBLOCK = 5;

	TokenType longType;
    int longLen;


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public LuaLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public LuaLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 1762) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead > 0) {
      zzEndRead+= numRead;
      return false;
    }
    // unlikely but not impossible: read 0 characters, but not at end of stream    
    if (numRead == 0) {
      int c = zzReader.read();
      if (c == -1) {
        return true;
      } else {
        zzBuffer[zzEndRead++] = (char) c;
        return false;
      }     
    }

	// numRead < 0
    return true;
  }

    
  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public Token yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      yychar+= zzMarkedPosL-zzStartRead;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 8: 
          { yybegin(STRING2);
                                    tokenStart = yychar;
                                    tokenLength = 1;
          }
        case 31: break;
        case 10: 
          { return token(TokenType.OPERATOR, -PARAN);
          }
        case 32: break;
        case 24: 
          { return token(TokenType.KEYWORD);
          }
        case 33: break;
        case 21: 
          { longType = TokenType.STRING;
                                   yybegin(LONGSTRING);
                                   tokenStart = yychar;
                                   tokenLength = yylength();
                                   longLen = tokenLength;
          }
        case 34: break;
        case 6: 
          { return token(TokenType.NUMBER);
          }
        case 35: break;
        case 16: 
          { yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 1);
          }
        case 36: break;
        case 20: 
          { yybegin(YYINITIAL);
									tokenLength += yylength();
                                    return token(TokenType.COMMENT, tokenStart, tokenLength);
          }
        case 37: break;
        case 3: 
          { return token(TokenType.OPERATOR);
          }
        case 38: break;
        case 11: 
          { return token(TokenType.OPERATOR,  CURLY);
          }
        case 39: break;
        case 12: 
          { return token(TokenType.OPERATOR, -CURLY);
          }
        case 40: break;
        case 13: 
          { tokenLength += yylength();
          }
        case 41: break;
        case 14: 
          { yybegin(YYINITIAL);
          }
        case 42: break;
        case 15: 
          { yybegin(YYINITIAL); 
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 1);
          }
        case 43: break;
        case 17: 
          { tokenLength++;
          }
        case 44: break;
        case 4: 
          { return token(TokenType.OPERATOR, -BRACKET);
          }
        case 45: break;
        case 9: 
          { return token(TokenType.OPERATOR,  PARAN);
          }
        case 46: break;
        case 5: 
          { return token(TokenType.IDENTIFIER);
          }
        case 47: break;
        case 7: 
          { yybegin(STRING1);
                                    tokenStart = yychar; 
                                    tokenLength = 1;
          }
        case 48: break;
        case 30: 
          { return token(TokenType.KEYWORD, REPEATBLOCK);
          }
        case 49: break;
        case 25: 
          { tokenLength += 2;
          }
        case 50: break;
        case 2: 
          { return token(TokenType.OPERATOR,  BRACKET);
          }
        case 51: break;
        case 26: 
          { if (longLen == yylength()) {
										tokenLength += yylength();
	                                    yybegin(YYINITIAL);
                                        return token(longType, tokenStart, tokenLength);
									 } else {
                                        tokenLength++;
									    yypushback(yylength() - 1);
                                     }
          }
        case 52: break;
        case 23: 
          { return token(TokenType.KEYWORD, ENDBLOCK);
          }
        case 53: break;
        case 19: 
          { yybegin(YYINITIAL);
                                    return token(TokenType.COMMENT, tokenStart, tokenLength);
          }
        case 54: break;
        case 18: 
          { yybegin(LINECOMMENT);
								   tokenLength += yylength();
          }
        case 55: break;
        case 22: 
          { yybegin(COMMENT);
                                   tokenStart = yychar;
                                   tokenLength = yylength();
          }
        case 56: break;
        case 29: 
          { return token(TokenType.KEYWORD, -REPEATBLOCK);
          }
        case 57: break;
        case 28: 
          { return token(TokenType.KEYWORD, -ENDBLOCK);
          }
        case 58: break;
        case 27: 
          { longType = TokenType.COMMENT;
                                   yybegin(LONGSTRING);
                                   tokenLength += yylength();
                                   longLen = yylength();
          }
        case 59: break;
        case 1: 
          { 
          }
        case 60: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            switch (zzLexicalState) {
            case COMMENT: {
              yybegin(YYINITIAL);
                                    return token(TokenType.COMMENT, tokenStart, tokenLength);
            }
            case 112: break;
            case LONGSTRING: {
              yybegin(YYINITIAL);
                                    return token(longType, tokenStart, tokenLength);
            }
            case 113: break;
            case STRING2: {
              yybegin(YYINITIAL);
                                    return token(TokenType.STRING, tokenStart, tokenLength);
            }
            case 114: break;
            case LINECOMMENT: {
              yybegin(YYINITIAL);
                                    return token(TokenType.COMMENT, tokenStart, tokenLength);
            }
            case 115: break;
            case STRING1: {
              yybegin(YYINITIAL);
                                    return token(TokenType.STRING, tokenStart, tokenLength);
            }
            case 116: break;
            default:
              {
                return null;
              }
            }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}