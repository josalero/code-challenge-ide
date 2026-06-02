use challenge::contains_duplicate;

#[test]
fn public_containsduplicate_1_2_3_1_should_be_true() {
    assert_eq!(contains_duplicate(&[1, 2, 3, 1]), true);
}
