// package trash;
//
// import org.lenteja.jdbc.query.IQueryObject;
// import org.lenteja.mapper.Column;
//
/// **
// * @param <S> (S)elf entity type
// * @param <R> (R)eferenced entity type
// * @param <C> (C)olumn type
// */
// public class JoinColumn<S, R, C> {
//
// public final Column<S, C> selfColumn;
// public final Column<R, C> refColumn;
//
// public JoinColumn(Column<S, C> selfColumn, Column<R, C> refColumn) {
// super();
// this.selfColumn = selfColumn;
// this.refColumn = refColumn;
// }
//
// @SuppressWarnings("unchecked")
// public IQueryObject getRestriction() {
// Column<S, Object> selfc = (Column<S, Object>) selfColumn;
// Column<R, Object> refc = (Column<R, Object>) refColumn;
// return refc.eq(selfc);
// }
//
// @SuppressWarnings("unchecked")
// public IQueryObject getRestriction(S entity) {
// Column<S, Object> selfc = (Column<S, Object>) selfColumn;
// Column<R, Object> refc = (Column<R, Object>) refColumn;
// return refc.eq(selfc.getAccessor().get(entity));
// }
//
// }