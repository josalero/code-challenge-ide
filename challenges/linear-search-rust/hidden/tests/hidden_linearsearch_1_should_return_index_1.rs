use challenge::linear_search;

#[test]
fn hidden_linearsearch_1_should_return_index_1() {
    assert_eq!(linear_search(&[], 1), -1);
}
